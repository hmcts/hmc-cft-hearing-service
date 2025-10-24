package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidManageHearingServiceException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequestResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_COMMIT_FAIL;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_COMMIT_FAIL_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MAX_HEARING_REQUESTS;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.EMPTY_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASE_REF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_EXCEPTION_ROLE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_HEARING_SERVICE_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LAST_GOOD_STATE_EMPTY;

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
    public ManageExceptionResponse manageExceptions(ManageExceptionRequest manageExceptionRequest,
                                                    String clientS2SToken) {
        String serviceName = securityUtils.getServiceNameFromS2SToken(clientS2SToken);
        validateServiceAndRole(serviceName);
        validateHearingIdLimit(manageExceptionRequest);
        validateUniqueHearingIds(manageExceptionRequest);

        final List<Long> hearingIds = extractHearingIds(manageExceptionRequest.getSupportRequests());
        final List<HearingEntity> hearingEntities = hearingRepository.getHearings(hearingIds);
        final Map<String, HearingEntity> byId = hearingEntities.stream()
            .collect(Collectors.toUnmodifiableMap(entity -> String.valueOf(entity.getId()),
                                                  Function.identity()));

        final List<SupportRequestResponse> responses = manageExceptionRequest.getSupportRequests().stream()
            .map(req -> processSingle(byId.get(req.getHearingId()), req, serviceName))
            .toList();

        ManageExceptionResponse response = new ManageExceptionResponse();
        response.setSupportRequestResponse(responses);
        return response;
    }

    @Transactional
    private SupportRequestResponse processSingle(HearingEntity entity,
                                                 SupportRequest request,
                                                 String serviceName) {
        if (entity == null) {
            return createResponse(request.getHearingId(),
                                  ManageRequestStatus.FAILURE.label, INVALID_HEARING_ID);
        }

        // Run validations; return first failure if any
        Optional<String> maybeError = validate(entity, request);
        if (maybeError.isPresent()) {
            return createResponse(request.getHearingId(), ManageRequestStatus.FAILURE.label, maybeError.get());
        }

        final String oldStatus = entity.getStatus();
        final ManageRequestAction action = actionFromLabel(request.getAction());
        final String newStatus = (action == ManageRequestAction.ROLLBACK)
            ? entity.getLastGoodStatus()
            : request.getState();

        try {
            saveHearingEntity(entity, newStatus);
            saveAuditEntity(request, entity, serviceName);
            return success(entity.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("DB commit failed for hearing ID {}: {}", entity.getId(), e.getMessage(), e);
            // Audit the error
            hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(
                entity, MANAGE_EXCEPTION_COMMIT_FAIL_EVENT, null, serviceName, HMC, null,
                objectMapper.convertValue("DB commit failed: " + e.getMessage(), JsonNode.class));
            return createResponse(String.valueOf(entity.getId()), ManageRequestStatus.FAILURE.label,
                                  MANAGE_EXCEPTION_COMMIT_FAIL);
        }
    }

    private Optional<String> validate(HearingEntity entity, SupportRequest req) {
        return firstPresent(
            () -> validateCaseRef(entity, req),
            () -> validateIsInException(entity, req),
            () -> validateFinalStateTransition(entity, req),
            () -> validateRollback(entity, req)
        );
    }

    public void validateServiceAndRole(String serviceName) {

        List<String> supportRoles = applicationParams.getAuthorisedSupportToolRoles();
        if (!applicationParams.getAuthorisedSupportToolServices().contains(serviceName)) {
            throw new InvalidManageHearingServiceException(INVALID_MANAGE_HEARING_SERVICE_EXCEPTION);
        }
        if (securityUtils.getUserInfo().getRoles().stream()
                .noneMatch(supportRoles::contains)) {
            throw new InvalidManageHearingServiceException(INVALID_MANAGE_EXCEPTION_ROLE);
        }
    }

    private Optional<String> validateCaseRef(HearingEntity entity, SupportRequest req) {
        if (!Objects.equals(req.getCaseRef(), entity.getLatestCaseReferenceNumber())) {
            log.info("Hearing ID: {} and case reference : {} do not match", req.getHearingId(), req.getCaseRef());
            return Optional.of(HEARING_ID_CASE_REF_MISMATCH);
        }
        return Optional.empty();
    }

    private Optional<String> validateIsInException(HearingEntity entity, SupportRequest req) {
        if (!HearingStatus.EXCEPTION.name().equals(entity.getStatus())) {
            log.info("Hearing ID: {} is not in EXCEPTION state. Current state: {}", req.getHearingId(),
                     entity.getStatus());
            return Optional.of(INVALID_HEARING_STATE);
        }
        return Optional.empty();
    }

    private Optional<String> validateFinalStateTransition(HearingEntity entity, SupportRequest req) {
        ManageRequestAction action = actionFromLabel(req.getAction());
        if (action == ManageRequestAction.FINAL_STATE_TRANSITION) {
            if (req.getState() == null || req.getState().isEmpty()) {
                log.info(
                    "Hearing ID: {} has Action : {} and state is empty or null : {}",
                    req.getHearingId(), req.getAction(), req.getState());
                return Optional.of(EMPTY_HEARING_STATE);
            }
            HearingStatus status = HearingStatus.valueOf(req.getState());
            if (!HearingStatus.isFinalStatus(status)) {
                log.info(
                    "Hearing ID: {} has Action : {} and invalid state transition request : {}",
                    req.getHearingId(), req.getAction(), req.getState());
                return Optional.of(INVALID_HEARING_ID_FINAL_STATE);
            }
            Optional<String> actualsError = getActuals(entity, req, status);
            if (actualsError.isPresent()) {
                return actualsError;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getActuals(HearingEntity entity, SupportRequest req, HearingStatus status) {
        if (status.equals(ADJOURNED) || status.equals(COMPLETED)) {
            log.info(
                "checking for hearing actuals for hearing id: {} with Action: {} and state transition request : {}",
                req.getHearingId(), req.getAction(), req.getState());
            val hearingResponses = entity.getLatestHearingResponse();
            if (hearingResponses.isEmpty() || hearingResponses.get().getActualHearingEntity() == null) {
                log.info(
                    "Hearing ID: {} has Action : {} and invalid state transition request : {} "
                        +  "as no hearing response found",
                    req.getHearingId(),
                    req.getAction(),
                    req.getState()
                );
                return Optional.of(HEARING_ACTUALS_NULL);
            }
        }
        return Optional.empty();
    }

    private Optional<String> validateRollback(HearingEntity entity, SupportRequest req) {
        ManageRequestAction action = actionFromLabel(req.getAction());
        if (action != ManageRequestAction.ROLLBACK) {
            return Optional.empty();
        }
        if (req.getState() != null) {
            log.info("Hearing ID: {} has Action : {} and invalid state transition request : {}",
                     req.getHearingId(), req.getAction(), req.getState());
            return Optional.of(INVALID_STATE);
        }
        if (entity.getLastGoodStatus() == null) {
            log.info("Hearing ID: {} does not have a last good state to roll back to", req.getHearingId());
            return Optional.of(LAST_GOOD_STATE_EMPTY);
        }
        if (!HearingStatus.isGoodStatus(HearingStatus.valueOf(entity.getLastGoodStatus()))) {
            log.info("Hearing ID: {} has invalid last good state to roll back to : {}",
                     req.getHearingId(), entity.getLastGoodStatus());
            return Optional.of(INVALID_LAST_GOOD_STATE);
        }
        return Optional.empty();
    }

    private void validateHearingIdLimit(ManageExceptionRequest manageExceptionRequest) {
        if (manageExceptionRequest.getSupportRequests().size() > MAX_HEARING_REQUESTS) {
            log.error("No. of hearings found in the request : {}", manageExceptionRequest.getSupportRequests().size());
            throw new BadRequestException(INVALID_HEARING_ID_LIMIT);
        }
    }

    private void validateUniqueHearingIds(ManageExceptionRequest manageExceptionRequest) {
        Set<String> uniqueHearingIds = new HashSet<>();
        for (SupportRequest request : manageExceptionRequest.getSupportRequests()) {
            String hearingId = request.getHearingId();
            if (!uniqueHearingIds.add(hearingId)) {
                log.error("Duplicate hearing ID found: {}", hearingId);
                throw new BadRequestException(DUPLICATE_HEARING_IDS);
            }
        }
    }

    private List<Long> extractHearingIds(List<SupportRequest> supportRequests) {
        try {
            return supportRequests.stream()
                .map(SupportRequest::getHearingId)
                .map(Long::valueOf)
                .toList();
        } catch (NumberFormatException e) {
            log.error("One or more hearingIds are not numeric");
            throw new BadRequestException(INVALID_HEARING_ID);
        }
    }

    private void saveAuditEntity(SupportRequest request, HearingEntity hearingEntity, String serviceName) {
        JsonNode otherInfo = objectMapper.convertValue(request.getNotes(), JsonNode.class);
        hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(hearingEntity, MANAGE_EXCEPTION_AUDIT_EVENT,
                                                                        null, serviceName, HMC,
                                                                        null, otherInfo);
    }

    private void saveHearingEntity(HearingEntity hearingEntity, String newStatus) {
        hearingEntity.setStatus(newStatus);
        hearingEntity.setErrorCode(null);
        hearingEntity.setErrorDescription(null);
        hearingRepository.save(hearingEntity);
        log.info("Hearing ID: {} updated to status: {}", hearingEntity.getId(), newStatus);
    }

    private SupportRequestResponse success(Long id, String from, String to) {
        log.info("hearing ID: {} has valid details", id);
        String successMessage = String.format(MANAGE_EXCEPTION_SUCCESS_MESSAGE, id, from, to);
        return createResponse(String.valueOf(id), ManageRequestStatus.SUCCESSFUL.label, successMessage);
    }

    private SupportRequestResponse createResponse(String hearingId, String status, String message) {
        SupportRequestResponse response = new SupportRequestResponse();
        response.setHearingId(hearingId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

    private ManageRequestAction actionFromLabel(String label) {
        for (ManageRequestAction a : ManageRequestAction.values()) {
            if (Objects.equals(a.label, label)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown ManageRequestAction label: " + label);
    }

    @SafeVarargs
    private static <T> Optional<T> firstPresent(Supplier<Optional<T>>... checks) {
        for (Supplier<Optional<T>> s : checks) {
            Optional<T> v = s.get();
            if (v.isPresent()) {
                return v;
            }
        }
        return Optional.empty();
    }

}
