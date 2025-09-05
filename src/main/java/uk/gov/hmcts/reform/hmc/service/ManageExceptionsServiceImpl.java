package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidManageHearingServiceException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequestResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASE_REF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_EXCEPTION_ROLE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_HEARING_SERVICE_EXCEPTION;

@Service
@Slf4j
public class ManageExceptionsServiceImpl implements ManageExceptionsService {

    private final HearingStatusAuditService hearingStatusAuditService;
    private final HearingRepository hearingRepository;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;
    private final ApplicationParams applicationParams;

    public ManageExceptionsServiceImpl(HearingStatusAuditService hearingStatusAuditService,
                                       HearingRepository hearingRepository, ObjectMapper objectMapper,
                                       SecurityUtils securityUtils, ApplicationParams applicationParams) {
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.hearingRepository = hearingRepository;
        this.objectMapper = objectMapper;
        this.securityUtils = securityUtils;
        this.applicationParams = applicationParams;
    }

    @Override
    @Transactional
    public ManageExceptionResponse manageExceptions(ManageExceptionRequest manageExceptionRequest,
                                                    String clientS2SToken) {
        validateServiceAndRole(clientS2SToken);
        validateHearingIdLimit(manageExceptionRequest);
        validateUniqueHearingIds(manageExceptionRequest);
        List<Long> hearingIds = extractHearingIds(manageExceptionRequest.getSupportRequests());
        List<HearingEntity> hearingEntities =  hearingRepository.getHearings(hearingIds);
        return processManageExceptionDetails(hearingEntities, manageExceptionRequest, clientS2SToken);
    }

    public void validateServiceAndRole(String clientS2SToken) {
        String serviceName = securityUtils.getServiceNameFromS2SToken(clientS2SToken);
        List<String> supportRoles = applicationParams.getAuthorisedSupportToolRoles();

        if (!applicationParams.getAuthorisedSupportToolServices().contains(serviceName)) {
            throw new InvalidManageHearingServiceException(INVALID_MANAGE_HEARING_SERVICE_EXCEPTION);
        }
        // Check if the user has the required IDAM role
        if (securityUtils.getUserInfo().getRoles().stream()
                .noneMatch(supportRoles::contains)) {
            throw new InvalidManageHearingServiceException(INVALID_MANAGE_EXCEPTION_ROLE);
        }
    }

    private ManageExceptionResponse processManageExceptionDetails(List<HearingEntity> hearingEntities,
                                                                  ManageExceptionRequest manageExceptionRequest,
                                                                  String clientS2SToken) {
        ManageExceptionResponse manageExceptionResponse = new ManageExceptionResponse();
        List<SupportRequestResponse> supportRequestResponseList = new ArrayList<>();
        for (SupportRequest request : manageExceptionRequest.getSupportRequests()) {
            String hearingId = request.getHearingId();

            Optional<HearingEntity> matchingHearingEntity = hearingEntities.stream()
                .filter(entity -> hearingId.equals(String.valueOf(entity.getId())))
                .findFirst();

            SupportRequestResponse response = new SupportRequestResponse();
            if (matchingHearingEntity.isPresent()) {
                HearingEntity entity = matchingHearingEntity.get();
                response = validateAndProcess(entity, request, response);
                if (!ManageRequestStatus.FAILURE.label.equals(response.getStatus())) {
                    String oldStatus = entity.getStatus();
                    String newStatus = request.getAction().equals(ManageRequestAction.ROLLBACK.label)
                            ? entity.getLastGoodStatus() : request.getState();
                    saveHearingEntity(entity, newStatus);
                    saveAuditEntity(request, entity, clientS2SToken);
                    response = hearingIsValid(entity, oldStatus, newStatus);
                }
            } else {
                response = createResponse(hearingId, ManageRequestStatus.FAILURE.label, INVALID_HEARING_ID);
            }
            supportRequestResponseList.add(response);
        }
        manageExceptionResponse.setSupportRequestResponse(supportRequestResponseList);
        return manageExceptionResponse;
    }

    private SupportRequestResponse validateAndProcess(HearingEntity entity, SupportRequest request,
                                              SupportRequestResponse response) {
        response = validateCaseRef(entity, request, response);
        if (isProcessingComplete(response)) {
            return response;
        }

        response = isHearingInExceptionState(entity, request, response);
        if (isProcessingComplete(response)) {
            return response;
        }

        response = validateHearingStatusForFinalStateTransition(entity, request, response);
        if (isProcessingComplete(response)) {
            return response;
        }

        response = validateRollBackAction(entity, request, response);
        if (isProcessingComplete(response)) {
            return response;
        }
        return response;
    }

    private SupportRequestResponse isHearingInExceptionState(HearingEntity hearingEntity, SupportRequest request,
                                                             SupportRequestResponse response) {
        if (!HearingStatus.EXCEPTION.name().equals(hearingEntity.getStatus())) {
            log.info(
                "Hearing ID: {} is not in EXCEPTION state. Current state: {}",
                request.getHearingId(),  hearingEntity.getStatus());
            response = createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label, INVALID_HEARING_STATE);
        }
        return response;
    }

    private SupportRequestResponse hearingIsValid(HearingEntity hearingEntity, String oldStatus, String newStatus) {
        log.info("hearing ID: {} has valid details", hearingEntity.getId());
        String successMessage = String.format(MANAGE_EXCEPTION_SUCCESS_MESSAGE,
                hearingEntity.getId(), oldStatus, newStatus);
        return createResponse(String.valueOf(hearingEntity.getId()),
                ManageRequestStatus.SUCCESSFUL.label, successMessage);
    }

    private boolean isProcessingComplete(SupportRequestResponse response) {
        return ManageRequestStatus.SUCCESSFUL.label.equals(response.getStatus())
            || ManageRequestStatus.FAILURE.label.equals(response.getStatus());
    }

    private SupportRequestResponse validateCaseRef(HearingEntity hearingEntity, SupportRequest request,
                                                   SupportRequestResponse response) {
        if (!request.getCaseRef().equals(hearingEntity.getLatestCaseReferenceNumber())) {
            log.info("Hearing ID: {} and case reference : {} do not match",
                     request.getHearingId(), request.getCaseRef());
            response = createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                  HEARING_ID_CASE_REF_MISMATCH);
        }
        return response;
    }

    private SupportRequestResponse validateRollBackAction(HearingEntity hearingEntity, SupportRequest request,
                                                          SupportRequestResponse response) {
        if (ManageRequestAction.ROLLBACK.label.equals(request.getAction())
            && hearingEntity.getLastGoodStatus() == null) {
            log.info("Hearing ID: {} does not have a last good state to roll back to", request.getHearingId());
            response = createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                      INVALID_LAST_GOOD_STATE);
        }
        return response;
    }

    private SupportRequestResponse validateHearingStatusForFinalStateTransition(HearingEntity hearingEntity,
                                                                                SupportRequest request,
                                                                                SupportRequestResponse response) {
        if (ManageRequestAction.FINAL_STATE_TRANSITION.label.equals(request.getAction())
            && !HearingStatus.isFinalStatus(HearingStatus.valueOf(request.getState()))) {
            log.info(
                "Hearing ID: {} has Action : {} and invalid state transition request : {}",
                request.getHearingId(), request.getAction(), request.getState());
            response = createResponse(
                request.getHearingId(), ManageRequestStatus.FAILURE.label, INVALID_HEARING_ID_FINAL_STATE);
        }
        return response;
    }

    private void validateHearingIdLimit(ManageExceptionRequest manageExceptionRequest) {
        if (manageExceptionRequest.getSupportRequests().size() > 100) {
            log.error("No. of hearings found in the request : {}", manageExceptionRequest.getSupportRequests().size());
            throw new HearingValidationException(INVALID_HEARING_ID_LIMIT);
        }
    }

    private void validateUniqueHearingIds(ManageExceptionRequest manageExceptionRequest) {
        Set<String> uniqueHearingIds = new HashSet<>();
        for (SupportRequest request : manageExceptionRequest.getSupportRequests()) {
            String hearingId = request.getHearingId();
            if (!uniqueHearingIds.add(hearingId)) {
                log.error("Duplicate hearing ID found: {}", hearingId);
                throw new HearingValidationException(DUPLICATE_HEARING_IDS);
            }
        }
    }

    private List<Long> extractHearingIds(List<SupportRequest> supportRequest) {
        return Collections.unmodifiableList(
            supportRequest.stream()
                .map(request -> Long.valueOf(request.getHearingId()))
                .toList());
    }

    private SupportRequestResponse createResponse(String hearingId, String status, String message) {
        SupportRequestResponse response = new SupportRequestResponse();
        response.setHearingId(hearingId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

    private void saveAuditEntity(SupportRequest request, HearingEntity hearingEntity, String clientS2SToken) {
        JsonNode otherInfo = objectMapper.convertValue(request.getNotes(), JsonNode.class);
        hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(hearingEntity, MANAGE_EXCEPTION_AUDIT_EVENT,
                                                                        null, clientS2SToken, HMC,
                                                                        null, otherInfo);
    }

    private void saveHearingEntity(HearingEntity hearingEntity, String newStatus) {
        hearingEntity.setStatus(newStatus);
        hearingEntity.setErrorCode(null);
        hearingEntity.setErrorDescription(null);
        hearingRepository.save(hearingEntity);
        log.info("Hearing ID: {} updated to status: {}", hearingEntity.getId(), newStatus);
    }

}
