package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToQueueConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.GetHearingsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.PartyRelationshipDetailsMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiDeleteHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiSubmitHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_DAY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_UN_EXPRECTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_AMEND_REASON_CODE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_RELATED_PARTY_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_DOW_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_RANGES_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private final DataStoreRepository dataStoreRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private final HearingMapper hearingMapper;
    private final GetHearingsResponseMapper getHearingsResponseMapper;
    private final GetHearingResponseMapper getHearingResponseMapper;
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;
    private final MessageSenderToQueueConfiguration messageSenderToQueueConfiguration;
    private final ApplicationParams applicationParams;
    private final HearingIdValidator hearingIdValidator;
    private final LinkedHearingValidator linkedHearingValidator;
    private final HearingRepository hearingRepository;
    private final PartyRelationshipDetailsMapper partyRelationshipDetailsMapper;

    @Autowired
    public HearingManagementServiceImpl(RoleAssignmentService roleAssignmentService, SecurityUtils securityUtils,
                                        @Qualifier("defaultDataStoreRepository")
                                                DataStoreRepository dataStoreRepository,
                                        HearingRepository hearingRepository,
                                        HearingMapper hearingMapper,
                                        CaseHearingRequestRepository caseHearingRequestRepository,
                                        HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper,
                                        GetHearingsResponseMapper getHearingsResponseMapper,
                                        GetHearingResponseMapper getHearingResponseMapper,
                                        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                        ObjectMapperService objectMapperService,
                                        HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper,
                                        MessageSenderToQueueConfiguration messageSenderToQueueConfiguration,
                                        ApplicationParams applicationParams,
                                        HearingIdValidator hearingIdValidator,
                                        LinkedHearingValidator linkedHearingValidator,
                                        PartyRelationshipDetailsMapper partyRelationshipDetailsMapper) {
        this.dataStoreRepository = dataStoreRepository;
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.hearingMapper = hearingMapper;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hmiSubmitHearingRequestMapper = hmiSubmitHearingRequestMapper;
        this.hmiDeleteHearingRequestMapper = hmiDeleteHearingRequestMapper;
        this.getHearingsResponseMapper = getHearingsResponseMapper;
        this.getHearingResponseMapper = getHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.messageSenderToQueueConfiguration = messageSenderToQueueConfiguration;
        this.applicationParams = applicationParams;
        this.hearingRepository = hearingRepository;
        this.hearingIdValidator = hearingIdValidator;
        this.linkedHearingValidator = linkedHearingValidator;
        this.partyRelationshipDetailsMapper = partyRelationshipDetailsMapper;
    }

    @Override
    public ResponseEntity<GetHearingResponse> getHearingRequest(Long hearingId, boolean isValid) {
        hearingIdValidator.isValidFormat(hearingId.toString());
        if (!hearingRepository.existsById(hearingId)) {
            throw new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND);
        } else if (!isValid) {
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            if (hearingEntity.isPresent()) {
                return ResponseEntity.ok(getHearingResponseMapper
                                             .toHearingResponse(hearingEntity.get()));
            } else {
                throw new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND);
            }
        } else {
            HearingEntity hearingEntity = hearingRepository.findById(hearingId)
                    .orElseThrow(() ->  new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
            return ResponseEntity.noContent()
                    .header("Latest-Hearing-Request-Version",
                            String.valueOf(hearingEntity.getLatestRequestVersion()))
                    .build();
        }
    }

    @Override
    @Transactional
    public HearingResponse saveHearingRequest(HearingRequest createHearingRequest) {
        if (createHearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(createHearingRequest);
        return insertHearingRequest(createHearingRequest);
    }

    @Override
    @Transactional
    public HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest) {
        validateHearingRequest(hearingRequest);
        hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateVersionNumber(hearingId, hearingRequest.getRequestDetails().getVersionNumber());
        validateHearingStatusForUpdate(hearingId);

        HearingEntity existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        String statusToUpdate = getNextPutHearingStatus(existingHearing.getStatus());
        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(hearingRequest, existingHearing, existingHearing.getNextRequestVersion(), statusToUpdate);
        savePartyRelationshipDetails(hearingRequest, hearingEntity);
        return getSaveHearingResponseDetails(hearingEntity);
    }

    @Override
    public void sendRequestToHmiAndQueue(Long hearingId, HearingRequest hearingRequest, String messageType) {
        HmiSubmitHearingRequest hmiSubmitHearingRequest = hmiSubmitHearingRequestMapper
            .mapRequest(hearingId, hearingRequest);
        sendRequestToQueue(hmiSubmitHearingRequest, hearingId, messageType);
    }

    @Override
    public void sendRequestToHmiAndQueue(DeleteHearingRequest hearingRequest, Long hearingId, String messageType) {
        HmiDeleteHearingRequest hmiDeleteHearingRequest = hmiDeleteHearingRequestMapper.mapRequest(hearingRequest);
        sendRequestToQueue(hmiDeleteHearingRequest, hearingId, messageType);
    }

    private void validateHearingStatusForUpdate(Long hearingId) {
        String status = getStatus(hearingId);
        if (!PutHearingStatus.isValid(status) || validatePlannedStartDate(hearingId,status)) {
            throw new BadRequestException(INVALID_PUT_HEARING_STATUS);
        }
    }

    private boolean validatePlannedStartDate(Long hearingId, String status) {

        val statusValues = Arrays.asList(
            HearingStatus.LISTED.toString(),
            HearingStatus.UPDATE_REQUESTED.toString(),
            HearingStatus.UPDATE_SUBMITTED.toString()
        );

        val existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(
                hearingId,
                HEARING_ID_NOT_FOUND
            ));
        if (existingHearing.hasHearingResponses()) {
            return statusValues.contains(status)
                && linkedHearingValidator.filterHearingResponses(existingHearing).isBefore(LocalDate.now());
        }
        return false;
    }

    /**
     * validate Get Hearing Request by caseRefId or caseRefId/caseStatus.
     * @param caseRef case Ref
     * @param status status
     * @return HearingRequest HearingRequest
     */
    @Override
    public GetHearingsResponse getHearings(String caseRef, String status) {
        log.info("caseRef:{} ; status:{}", caseRef, status);
        List<CaseHearingRequestEntity> entities;
        if (!isBlank(status)) {
            entities = caseHearingRequestRepository.getHearingDetailsWithStatus(caseRef, status);
        } else {
            entities = caseHearingRequestRepository.getHearingDetails(caseRef);
        }
        return getHearingsResponseMapper.toHearingsResponse(caseRef, entities);
    }

    @Override
    public ResponseEntity hearingCompletion(Long hearingId) {
        hearingIdValidator.validateHearingId(hearingId, HEARING_ACTUALS_ID_NOT_FOUND);
        linkedHearingValidator.validateHearingActualsStatus(hearingId, HEARING_ACTUALS_INVALID_STATUS);
        hearingIdValidator.validateHearingOutcomeInformation(hearingId, HEARING_ACTUALS_MISSING_HEARING_OUTCOME);
        hearingIdValidator.validateHearingResultType(hearingId, HEARING_ACTUALS_MISSING_HEARING_DAY);
        hearingIdValidator.validateCancelHearingResultType(hearingId, HEARING_ACTUALS_UN_EXPRECTED);
        updateStatus(hearingId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private void updateStatus(Long hearingId) {
        ActualHearingEntity actualHearingEntity = hearingIdValidator.getActualHearing(hearingId)
            .orElseThrow(() -> new BadRequestException(HEARING_ACTUALS_MISSING_HEARING_OUTCOME));
        HearingEntity hearingEntity = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        hearingEntity.setStatus(actualHearingEntity.getHearingResultType().getLabel());
        hearingRepository.save(hearingEntity);
    }

    private String getNextPutHearingStatus(String currentStatus) {
        if (PutHearingStatus.HEARING_REQUESTED.name().equals(currentStatus)) {
            return PutHearingStatus.HEARING_REQUESTED.name();
        } else {
            return PutHearingStatus.UPDATE_REQUESTED.name();
        }
    }

    private HearingResponse insertHearingRequest(HearingRequest createHearingRequest) {
        HearingEntity savedEntity = saveHearingDetails(createHearingRequest);
        savePartyRelationshipDetails(createHearingRequest, savedEntity);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity saveHearingDetails(HearingRequest createHearingRequest) {
        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(createHearingRequest, new HearingEntity(), VERSION_NUMBER_TO_INCREMENT, POST_HEARING_STATUS);
        return hearingRepository.save(hearingEntity);
    }

    private void savePartyRelationshipDetails(HearingRequest hearingRequest, HearingEntity hearingEntity) {

        final List<PartyDetails> partyDetails = hearingRequest.getPartyDetails();
        final List<HearingPartyEntity> hearingParties = hearingEntity.getLatestCaseHearingRequest().getHearingParties();


        if (!CollectionUtils.isEmpty(partyDetails) && !CollectionUtils.isEmpty(hearingParties)) {
            for (PartyDetails partyDetail : partyDetails) {
                if (partyDetail.getIndividualDetails() != null
                    && !CollectionUtils.isEmpty(partyDetail.getIndividualDetails().getRelatedParties())) {
                    partyRelationshipDetailsMapper.modelToEntity(partyDetail, hearingParties);
                }
            }
        }
    }

    private HearingResponse getSaveHearingResponseDetails(HearingEntity savedEntity) {
        HearingResponse hearingResponse = new HearingResponse();
        if (null != savedEntity) {
            CaseHearingRequestEntity latestCaseHearingRequest = savedEntity.getLatestCaseHearingRequest();
            hearingResponse.setHearingRequestId(savedEntity.getId());
            hearingResponse.setTimeStamp(latestCaseHearingRequest.getHearingRequestReceivedDateTime());
            hearingResponse.setStatus(savedEntity.getStatus());
            hearingResponse.setVersionNumber(latestCaseHearingRequest.getVersionNumber());
        }
        return hearingResponse;
    }

    private void validateHearingRequest(HearingRequest createHearingRequest) {
        validateHearingRequestDetails(createHearingRequest);
        validateHearingDetails(createHearingRequest.getHearingDetails());
        if (createHearingRequest.getPartyDetails() != null) {
            validatePartyDetails(createHearingRequest.getPartyDetails());
        }
    }

    private void validateHearingRequest(UpdateHearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        validateAmendReasonCodeForUpdate(hearingRequest.getHearingDetails().getAmendReasonCode());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }
    }

    private void validateAmendReasonCodeForUpdate(String amendReasonCode) {
        if (amendReasonCode == null || amendReasonCode.isEmpty()) {
            throw new BadRequestException(INVALID_AMEND_REASON_CODE);
        }
    }

    private void validatePartyDetails(List<PartyDetails> partyDetails) {
        for (PartyDetails partyDetail : partyDetails) {
            if ((partyDetail.getIndividualDetails() != null && partyDetail.getOrganisationDetails() != null)
                || (partyDetail.getIndividualDetails() == null && partyDetail.getOrganisationDetails() == null)) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (partyDetail.getUnavailabilityDow() != null && partyDetail.getUnavailabilityDow().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_DOW_DETAILS);
            }
            if (partyDetail.getUnavailabilityRanges() != null && partyDetail.getUnavailabilityRanges().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_RANGES_DETAILS);
            }
            if (partyDetail.getIndividualDetails() != null
                && (partyDetail.getIndividualDetails().getRelatedParties() != null
                && partyDetail.getIndividualDetails().getRelatedParties().isEmpty())) {
                throw new BadRequestException(INVALID_RELATED_PARTY_DETAILS);
            }
        }
    }

    private void validateHearingRequestDetails(HearingRequest createHearingRequest) {
        if (createHearingRequest.getRequestDetails() == null
            && createHearingRequest.getHearingDetails() == null
            && createHearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
    }

    private void validateHearingRequestDetails(UpdateHearingRequest hearingRequest) {
        if (hearingRequest.getRequestDetails() == null
            && hearingRequest.getHearingDetails() == null
            && hearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
    }

    private void validateHearingDetails(HearingDetails hearingDetails) {
        if (hearingDetails.getHearingWindow().getDateRangeEnd() == null
            && hearingDetails.getHearingWindow().getDateRangeStart() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(INVALID_HEARING_WINDOW);
        }
    }

    public void verifyAccess(String caseReference) {
        if (!applicationParams.isAccessControlEnabled()) {
            return;
        }
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());
        if (roleAssignments.getRoleAssignments().isEmpty()) {
            throw new ResourceNotFoundException(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, securityUtils.getUserId()));
        }
        List<RoleAssignment> filteredRoleAssignments = new ArrayList<>();
        for (RoleAssignment roleAssignment : roleAssignments.getRoleAssignments()) {
            if (roleAssignment.getRoleName().equalsIgnoreCase("Hearing Manage")
                && roleAssignment.getRoleType().equalsIgnoreCase(
                "ORGANISATION")) {
                filteredRoleAssignments.add(roleAssignment);
            }
        }
        if (filteredRoleAssignments.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ROLE);
        } else {
            DataStoreCaseDetails caseDetails;
            caseDetails = dataStoreRepository.findCaseByCaseIdUsingExternalApi(caseReference);
            if (!checkRoleAssignmentMatchesCaseDetails(caseDetails, filteredRoleAssignments)) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES);
            }
        }
    }

    @SuppressWarnings("java:S2789")
    private boolean checkRoleAssignmentMatchesCaseDetails(DataStoreCaseDetails caseDetails,
                                                          List<RoleAssignment> roleAssignments) {
        for (RoleAssignment roleAssignment : roleAssignments) {
            RoleAssignmentAttributes attributes = roleAssignment.getAttributes();
            if (attributes.getJurisdiction() == null) {
                return ifJurisdictionIsNullOrEmpty(attributes, caseDetails);
            } else if (attributes.getJurisdiction().isEmpty()) {
                return ifJurisdictionIsNullOrEmpty(attributes, caseDetails);
            } else if (attributes.getJurisdiction().equals(Optional.of(caseDetails.getJurisdiction()))) {
                return true;
            } else if (attributes.getCaseType() != null && attributes.getCaseType().isPresent() && attributes
                .getCaseType().equals(Optional.of(caseDetails.getCaseTypeId()))) {
                return true;
            }

        }
        return false;
    }

    @SuppressWarnings("java:S2789")
    private boolean ifJurisdictionIsNullOrEmpty(RoleAssignmentAttributes attributes, DataStoreCaseDetails caseDetails) {
        if (attributes.getCaseType() == null) {
            return true;
        } else if (attributes.getCaseType().isEmpty()) {
            return true;
        } else {
            return attributes.getCaseType().equals(Optional.of(caseDetails.getCaseTypeId()));
        }
    }

    @Override
    @Transactional
    public HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest) {
        hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateDeleteHearingStatus(hearingId);

        HearingEntity existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(deleteRequest, existingHearing, existingHearing.getNextRequestVersion());
        return getSaveHearingResponseDetails(hearingEntity);
    }

    private void validateDeleteHearingStatus(Long hearingId) {
        String status = getStatus(hearingId);
        if (!DeleteHearingStatus.isValid(status)) {
            throw new BadRequestException(INVALID_DELETE_HEARING_STATUS);
        }
    }

    private String getStatus(Long hearingId) {
        return hearingRepository.getStatus(hearingId);
    }

    private void validateVersionNumber(Long hearingId, Integer versionNumber) {
        Integer latestVersionNumberFromDb = getLatestVersionNumber(hearingId);
        if (!latestVersionNumberFromDb.equals(versionNumber)) {
            throw new BadRequestException(INVALID_VERSION_NUMBER);
        }
    }

    private Integer getLatestVersionNumber(Long hearingId) {
        return caseHearingRequestRepository.getLatestVersionNumber(hearingId);
    }

    @Override
    public void sendResponse(String json) {
        sendRspToTopic(json);
    }

    private void sendRspToTopic(Object response) {
        var jsonNode = objectMapperService.convertObjectToJsonNode(response);
        messageSenderToTopicConfiguration.sendMessage(jsonNode.toString());
    }

    private void sendRequestToQueue(HmiSubmitHearingRequest hmiSubmitHearingRequest, Long hearingId,
                                    String messageType) {
        var jsonNode = objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest);
        messageSenderToQueueConfiguration.sendMessageToQueue(jsonNode.toString(), hearingId, messageType);
    }

    private void sendRequestToQueue(HmiDeleteHearingRequest hmiDeleteHearingRequest, Long hearingId,
                                    String messageType) {
        var jsonNode = objectMapperService.convertObjectToJsonNode(hmiDeleteHearingRequest);
        messageSenderToQueueConfiguration.sendMessageToQueue(jsonNode.toString(), hearingId, messageType);
    }
}
