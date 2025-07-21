package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequestResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.ManageExceptionRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASEREF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_INCORRECT_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_SERVICE_TOKEN;

@Service
@Slf4j
public class ManageExceptionsServiceImpl implements ManageExceptionsService {

    private final ManageExceptionRepository manageExceptionRepository;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final HearingRepository hearingRepository;
    private final ObjectMapper objectMapper;

    public ManageExceptionsServiceImpl(ManageExceptionRepository manageExceptionRepository,
                                       HearingStatusAuditService hearingStatusAuditService,
                                       HearingRepository hearingRepository,
                                       ObjectMapper objectMapper) {
        this.manageExceptionRepository = manageExceptionRepository;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.hearingRepository = hearingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ManageExceptionResponse manageExceptions(ManageExceptionRequest manageExceptionRequest,
                                                    String clientS2SToken) {
        validateServiceToken(clientS2SToken);
        validateHearingIdLimit(manageExceptionRequest);
        validateUniqueHearingIds(manageExceptionRequest);
        ManageExceptionResponse manageExceptionResponse = processManageExceptionDetails(manageExceptionRequest,
                                                                                        clientS2SToken);
        return manageExceptionResponse;
    }

    private void validateServiceToken(String clientS2SToken) {
        // This method is a placeholder for actual service token validation logic.
        if (clientS2SToken == null || clientS2SToken.isEmpty()) {
            log.error(INVALID_SERVICE_TOKEN + " : {}", clientS2SToken);
            throw new InvalidRoleAssignmentException(INVALID_SERVICE_TOKEN);
        }
    }

    private void validateHearingIdLimit(ManageExceptionRequest manageExceptionRequest) {
        if (manageExceptionRequest.getSupportRequest().size() > 100) {
            log.error("No. of hearings found in the request : {}", manageExceptionRequest.getSupportRequest().size());
            throw new HearingValidationException(INVALID_HEARING_ID_LIMIT);
        }
    }

    private void validateUniqueHearingIds(ManageExceptionRequest manageExceptionRequest) {
        Set<String> uniqueHearingIds = new HashSet<>();
        for (SupportRequest request : manageExceptionRequest.getSupportRequest()) {
            String hearingId = request.getHearingId();
            if (!uniqueHearingIds.add(hearingId)) {
                log.error("Duplicate hearing ID found: {}", hearingId);
                throw new HearingValidationException(DUPLICATE_HEARING_IDS);
            }
        }
    }

    private ManageExceptionResponse processManageExceptionDetails(ManageExceptionRequest manageExceptionRequest,
                                                                  String clientS2SToken) {
        ManageExceptionResponse manageExceptionResponse = new ManageExceptionResponse();
        List<SupportRequestResponse> supportRequestResponseList = new ArrayList<>();

        for (SupportRequest request : manageExceptionRequest.getSupportRequest()) {
            SupportRequestResponse response = processHearingRequest(request, clientS2SToken);
            if (response != null) {
                supportRequestResponseList.add(response);
            }
        }
        manageExceptionResponse.setSupportRequestResponse(supportRequestResponseList);
        return manageExceptionResponse;
    }

    private SupportRequestResponse processHearingRequest(SupportRequest request, String clientS2SToken) {
        HearingEntity hearingEntity = manageExceptionRepository.findByHearingId(Long.valueOf(request.getHearingId()));

        if (hearingEntity == null) {
            log.info("Hearing ID: {} not found in the database", request.getHearingId());
            return createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                  INVALID_LAST_GOOD_STATE);
        }

        if (!request.getCaseRef().equals(hearingEntity.getLatestCaseReferenceNumber())) {
            log.info("Hearing ID: {} and case reference : {} do not match",
                     request.getHearingId(), request.getCaseRef());
            return createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                  HEARING_ID_CASEREF_MISMATCH);
        }

        if (! HearingStatus.EXCEPTION.equals(hearingEntity.getStatus())) {
            log.info("Hearing ID: {} is in incorrect state: {}", request.getHearingId(), hearingEntity.getStatus());
            return createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                  HEARING_ID_INCORRECT_STATE);
        }

        if (ManageRequestAction.ROLL_BACK.equals(request.getAction()) && hearingEntity.getLastGoodStatus() == null) {
            log.info("Hearing ID: {} has no last good state to roll back to", request.getHearingId());
            return createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label,
                                  INVALID_LAST_GOOD_STATE);
        }

        saveHearingEntity(hearingEntity, request.getState());
        saveAuditEntity(request, hearingEntity, clientS2SToken);
        String successMessage = String.format("%s, %s, %s, %s",
                                              MANAGE_EXCEPTION_SUCCESS_MESSAGE,
                                              hearingEntity.getId(),
                                              hearingEntity.getStatus(),
                                              request.getState());
        return createResponse(request.getHearingId(), ManageRequestStatus.SUCCESSFUL.label, successMessage);
    }

    private void saveAuditEntity(SupportRequest request, HearingEntity hearingEntity, String clientS2SToken) {
        JsonNode otherInfo = objectMapper.convertValue(request.getNotes(), JsonNode.class);
        // set ResponseDateTime to null or localdateTime.now() if needed
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingEntity, MANAGE_EXCEPTION_AUDIT_EVENT,
                                                                        null, clientS2SToken, HMC,
                                                                        null, otherInfo);
    }

    private void saveHearingEntity(HearingEntity hearingEntity, String newStatus) {
        hearingEntity.setStatus(newStatus);
        // to set error details to null
        hearingRepository.save(hearingEntity);
        log.info("Hearing ID: {} updated to status: {}", hearingEntity.getId(), newStatus);
    }

    private SupportRequestResponse createResponse(String hearingId, String status, String message) {
        SupportRequestResponse response = new SupportRequestResponse();
        response.setHearingId(hearingId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

}
