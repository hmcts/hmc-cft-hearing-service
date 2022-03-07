package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToQueueConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.GetHearingsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiDeleteHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiSubmitHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
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
import uk.gov.hmcts.reform.hmc.repository.CancellationReasonsRepository;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.transaction.Transactional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
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
@Component
@Slf4j
public class HearingManagementServiceImpl extends HearingIdValidator implements HearingManagementService {

    private final DataStoreRepository dataStoreRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private final HearingMapper hearingMapper;
    private final CancellationReasonsRepository cancellationReasonsRepository;
    private final GetHearingsResponseMapper getHearingsResponseMapper;
    private final GetHearingResponseMapper getHearingResponseMapper;
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;
    private final MessageSenderToQueueConfiguration messageSenderToQueueConfiguration;

    @Autowired
    public HearingManagementServiceImpl(RoleAssignmentService roleAssignmentService, SecurityUtils securityUtils,
                                        @Qualifier("defaultDataStoreRepository")
                                            DataStoreRepository dataStoreRepository,
                                        HearingRepository hearingRepository,
                                        HearingMapper hearingMapper,
                                        CaseHearingRequestRepository caseHearingRequestRepository,
                                        CancellationReasonsRepository cancellationReasonsRepository,
                                        HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper,
                                        GetHearingsResponseMapper getHearingsResponseMapper,
                                        GetHearingResponseMapper getHearingResponseMapper,
                                        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                        ObjectMapperService objectMapperService,
                                        HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper,
                                        MessageSenderToQueueConfiguration messageSenderToQueueConfiguration) {
        super(hearingRepository);
        this.dataStoreRepository = dataStoreRepository;
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.hearingMapper = hearingMapper;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.cancellationReasonsRepository = cancellationReasonsRepository;
        this.hmiSubmitHearingRequestMapper = hmiSubmitHearingRequestMapper;
        this.hmiDeleteHearingRequestMapper = hmiDeleteHearingRequestMapper;
        this.getHearingsResponseMapper = getHearingsResponseMapper;
        this.getHearingResponseMapper = getHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.messageSenderToQueueConfiguration = messageSenderToQueueConfiguration;
    }

    @Override
    public ResponseEntity<GetHearingResponse> getHearingRequest(Long hearingId, boolean isValid) {
        isValidFormat(hearingId.toString());
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
            return ResponseEntity.noContent().header("Content-Length", "0").build();
        }
    }

    @Override
    @Transactional
    public HearingResponse saveHearingRequest(CreateHearingRequest createHearingRequest) {
        if (createHearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(createHearingRequest);
        return insertHearingRequest(createHearingRequest);
    }

    @Override
    public HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest) {
        validateHearingRequest(hearingRequest);
        validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateVersionNumber(hearingId, hearingRequest.getRequestDetails().getVersionNumber());
        validateHearingStatusForUpdate(hearingId);

        // TODO: What's the next Status?!
        HearingEntity savedEntity = updateHearingStatusAndVersionNumber(hearingId, null);
        return getSaveHearingResponseDetails(savedEntity);
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
        if (!PutHearingStatus.isValid(status)) {
            throw new BadRequestException(INVALID_PUT_HEARING_STATUS);
        }
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

    private HearingResponse insertHearingRequest(CreateHearingRequest createHearingRequest) {
        HearingEntity savedEntity = saveHearingDetails(createHearingRequest);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity saveHearingDetails(CreateHearingRequest createHearingRequest) {
        HearingEntity hearingEntity = hearingMapper.modelToEntity(createHearingRequest);
        return hearingRepository.save(hearingEntity);
    }

    private HearingResponse getSaveHearingResponseDetails(HearingEntity savedEntity) {
        log.info("Hearing details saved successfully with id: {}", savedEntity.getId());
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingRequestId(savedEntity.getId());
        hearingResponse.setTimeStamp(savedEntity.getCaseHearingRequest().getHearingRequestReceivedDateTime());
        hearingResponse.setStatus(savedEntity.getStatus());
        hearingResponse.setVersionNumber(savedEntity.getCaseHearingRequest().getVersionNumber());
        return hearingResponse;
    }

    private void validateHearingRequest(CreateHearingRequest createHearingRequest) {
        validateHearingRequestDetails(createHearingRequest);
        validateHearingDetails(createHearingRequest.getHearingDetails());
        if (createHearingRequest.getPartyDetails() != null) {
            validatePartyDetails(createHearingRequest.getPartyDetails());
        }
    }

    private void validateHearingRequest(UpdateHearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
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

    private void validateHearingRequestDetails(CreateHearingRequest createHearingRequest) {
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
        if (hearingDetails.getHearingWindow().getHearingWindowEndDateRange() == null
            && hearingDetails.getHearingWindow().getHearingWindowStartDateRange() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(INVALID_HEARING_WINDOW);
        }
    }

    public void verifyAccess(String caseReference) {
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
    public HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest) {
        validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateVersionNumber(hearingId, deleteRequest.getVersionNumber());
        validateDeleteHearingStatus(hearingId);
        updateCancellationReasons(hearingId, deleteRequest.getCancellationReasonCode());
        HearingEntity savedEntity = updateHearingStatusAndVersionNumber(
            hearingId, CANCELLATION_REQUESTED);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity updateHearingStatusAndVersionNumber(Long hearingId, String newStatus) {

        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);

        if (hearingResult.isPresent()) {
            final HearingEntity hearingEntity = hearingResult.get();
            if (null != hearingEntity.getCaseHearingRequest()) {
                log.info("CHANGING: version number : {}", hearingEntity.getCaseHearingRequest().getVersionNumber());
                hearingEntity.getCaseHearingRequest().setVersionNumber(
                    hearingEntity.getCaseHearingRequest().getVersionNumber() + 1);
                log.info(
                    "TO: version number : {}",
                    hearingEntity.getCaseHearingRequest().getVersionNumber()
                );
                if (null != newStatus) {
                    log.info("CHANGING: Hearing status {}", hearingEntity.getStatus());
                    hearingEntity.setStatus(newStatus);
                    log.info("TO: Hearing status {}", hearingEntity.getStatus());
                }
            } else {
                log.info("Unable to set status & version number - due to NULL caseHearingRequest!!!");
            }
            hearingRepository.save(hearingEntity);
            return hearingEntity;
        } else {
            throw new NoSuchElementException();
        }

    }

    private void updateCancellationReasons(Long hearingId, String cancellationReasonCode) {
        CaseHearingRequestEntity caseHearingRequestEntity = getCaseHearing(hearingId);
        final CancellationReasonsEntity cancellationReasonsEntity = setCancellationReasonsEntity(
            cancellationReasonCode, caseHearingRequestEntity);
        cancellationReasonsRepository.save(cancellationReasonsEntity);
    }

    private CaseHearingRequestEntity getCaseHearing(Long hearingId) {
        return caseHearingRequestRepository.getCaseHearing(hearingId);
    }

    private CancellationReasonsEntity setCancellationReasonsEntity(String cancellationReasonCode,
                                                                   CaseHearingRequestEntity caseHearingRequestEntity) {
        final CancellationReasonsEntity cancellationReasonsEntity = new CancellationReasonsEntity();
        cancellationReasonsEntity.setCaseHearing(caseHearingRequestEntity);
        cancellationReasonsEntity.setCancellationReasonType(cancellationReasonCode);
        return cancellationReasonsEntity;
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
        Integer versionNumberFromDb = getVersionNumber(hearingId);
        if (!versionNumberFromDb.equals(versionNumber)) {
            throw new BadRequestException(INVALID_VERSION_NUMBER);
        }
    }

    private Integer getVersionNumber(Long hearingId) {
        return caseHearingRequestRepository.getVersionNumber(hearingId);
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
