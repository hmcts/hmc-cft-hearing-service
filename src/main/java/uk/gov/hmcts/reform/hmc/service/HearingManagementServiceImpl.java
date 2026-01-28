package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.helper.ElasticSearchQuery;
import uk.gov.hmcts.reform.hmc.helper.GetHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.GetHearingsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.PartyRelationshipDetailsMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.EntitiesMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.EntitiesMapperObject;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiCaseDetailsMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiDeleteHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiSubmitHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.ListingMapper;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingActualsValidator;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LATEST_HEARING_REQUEST_VERSION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LATEST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NO_DEFINED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_DETAILS_ARE_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_EMPTY_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_AMEND_REASON_CODE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MISSING_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MISSING_ORGANISATION_DETAILS;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private final DataStoreRepository dataStoreRepository;
    private final HearingMapper hearingMapper;
    private final GetHearingsResponseMapper getHearingsResponseMapper;
    private final GetHearingResponseMapper getHearingResponseMapper;
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;
    private final HearingIdValidator hearingIdValidator;
    private final HearingActualsValidator hearingActualsValidator;
    private final LinkedHearingValidator linkedHearingValidator;
    private final HearingRepository hearingRepository;
    private final PartyRelationshipDetailsMapper partyRelationshipDetailsMapper;
    private final ListingMapper listingMapper;
    private final HmiCaseDetailsMapper hmiCaseDetailsMapper;
    private final EntitiesMapper entitiesMapper;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final PendingRequestService pendingRequestService;


    @Autowired
    public HearingManagementServiceImpl(@Qualifier("defaultDataStoreRepository")
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
                                        HearingIdValidator hearingIdValidator,
                                        LinkedHearingValidator linkedHearingValidator,
                                        PartyRelationshipDetailsMapper partyRelationshipDetailsMapper,
                                        HearingActualsValidator hearingActualsValidator,
                                        ListingMapper listingMapper,
                                        HmiCaseDetailsMapper hmiCaseDetailsMapper,
                                        EntitiesMapper entitiesMapper,
                                        HmiHearingResponseMapper hmiHearingResponseMapper,
                                        HearingStatusAuditService hearingStatusAuditService,
                                        PendingRequestService pendingRequestService) {
        this.dataStoreRepository = dataStoreRepository;
        this.hearingMapper = hearingMapper;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hmiSubmitHearingRequestMapper = hmiSubmitHearingRequestMapper;
        this.hmiDeleteHearingRequestMapper = hmiDeleteHearingRequestMapper;
        this.getHearingsResponseMapper = getHearingsResponseMapper;
        this.getHearingResponseMapper = getHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.hearingRepository = hearingRepository;
        this.hearingIdValidator = hearingIdValidator;
        this.linkedHearingValidator = linkedHearingValidator;
        this.partyRelationshipDetailsMapper = partyRelationshipDetailsMapper;
        this.hearingActualsValidator = hearingActualsValidator;
        this.listingMapper = listingMapper;
        this.hmiCaseDetailsMapper = hmiCaseDetailsMapper;
        this.entitiesMapper = entitiesMapper;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.pendingRequestService = pendingRequestService;
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
                .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(LATEST_HEARING_REQUEST_VERSION,
                                String.valueOf(hearingEntity.getLatestRequestVersion()));
            responseHeaders.set(LATEST_HEARING_STATUS,
                                String.valueOf(hearingEntity.getStatus()));
            return ResponseEntity.noContent()
                .headers(responseHeaders)
                .build();
        }
    }

    @Override
    @Transactional
    public HearingResponse saveHearingRequest(HearingRequest createHearingRequest, String deploymentId,
                                              String clientS2SToken) {
        if (createHearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(createHearingRequest);
        EntitiesMapperObject entities = entitiesMapper.getEntities(createHearingRequest.getPartyDetails());
        Listing listing = getListing(createHearingRequest, entities);
        boolean reasonableMatch = listingMapper.checkRoomAttributesByReasonableAdjustmentCode(entities.getEntities());
        List<String> facilitiesRoomAttributes =
            listingMapper.getRoomAttributesByAttributeCode(createHearingRequest
                                                               .getHearingDetails().getFacilitiesRequired());
        int size = (createHearingRequest.getHearingDetails().getFacilitiesRequired() == null)
            ? 0 : createHearingRequest.getHearingDetails().getFacilitiesRequired().size();
        boolean facilitiesMatch = (facilitiesRoomAttributes.size() == size);

        HearingResponse hearingResponse = insertHearingRequest(createHearingRequest, reasonableMatch, facilitiesMatch,
                                                               deploymentId, clientS2SToken);
        sendRequestToHmiAndQueue(hearingResponse.getHearingRequestId(), REQUEST_HEARING, createHearingRequest,
                                    getCaseDetails(hearingResponse.getHearingRequestId(), createHearingRequest),
                                    listing, deploymentId
        );
        return hearingResponse;
    }

    @Override
    @Transactional
    public HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest,
                                                 String deploymentId, String clientS2SToken) {
        validateHearingRequest(hearingRequest);
        hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateVersionNumber(hearingId, hearingRequest.getRequestDetails().getVersionNumber());
        validateHearingStatusForUpdate(hearingId);

        HearingEntity existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        if (!existingHearing.getLatestCaseReferenceNumber().equals(hearingRequest.getCaseDetails().getCaseRef())) {
            throw new BadRequestException(INVALID_CASE_REFERENCE);
        }
        final int existingRequestVersion = existingHearing.getLatestRequestVersion();
        String statusToUpdate = getNextPutHearingStatus(existingHearing.getStatus());
        EntitiesMapperObject entities = entitiesMapper.getEntities(hearingRequest.getPartyDetails());
        boolean reasonableMatch = listingMapper.checkRoomAttributesByReasonableAdjustmentCode(entities.getEntities());
        List<String> facilitiesRoomAttributes =
            listingMapper.getRoomAttributesByAttributeCode(hearingRequest.getHearingDetails().getFacilitiesRequired());
        int size = (hearingRequest.getHearingDetails().getFacilitiesRequired() == null)
            ? 0 : hearingRequest.getHearingDetails().getFacilitiesRequired().size();
        boolean facilitiesMatch = (facilitiesRoomAttributes.size() == size);

        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(hearingRequest, existingHearing, existingHearing.getNextRequestVersion(), statusToUpdate,
                           reasonableMatch, facilitiesMatch, deploymentId);

        auditChangeInRequestVersion(hearingEntity, existingRequestVersion, clientS2SToken, false);

        savePartyRelationshipDetails(hearingRequest, hearingEntity);
        HearingResponse saveHearingResponseDetails = getSaveHearingResponseDetails(hearingEntity);

        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(UPDATE_HEARING_REQUEST)
                .httpStatus(String.valueOf(HttpStatus.OK.value()))
                .source(clientS2SToken)
                .target(HMC)
                .useCurrentTimestamp(false)
                .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
        Listing listing = getListing(hearingRequest, entities);
        sendRequestToHmiAndQueue(saveHearingResponseDetails.getHearingRequestId(), AMEND_HEARING, hearingRequest,
            getCaseDetails(saveHearingResponseDetails.getHearingRequestId(), hearingRequest), listing, deploymentId);
        return saveHearingResponseDetails;
    }

    @Override
    @Transactional
    public HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest,
                                                String clientS2SToken) {
        hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
        validateDeleteHearingStatus(hearingId);

        HearingEntity existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        final int existingRequestVersion = existingHearing.getLatestRequestVersion();
        CaseHearingRequestEntity caseHearingRequestEntity = existingHearing.getLatestCaseHearingRequest();
        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(deleteRequest, existingHearing, existingHearing.getNextRequestVersion(),
                           caseHearingRequestEntity);

        auditChangeInRequestVersion(hearingEntity, existingRequestVersion, clientS2SToken, true);

        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(DELETE_HEARING_REQUEST)
                .httpStatus(String.valueOf(HttpStatus.OK.value()))
                .source(clientS2SToken)
                .target(HMC)
                .useCurrentTimestamp(true)
                .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
        HearingResponse saveHearingResponseDetails = getSaveHearingResponseDetails(hearingEntity);
        generatePendingRequest(hearingId, DELETE_HEARING, existingHearing.getDeploymentId());
        return saveHearingResponseDetails;
    }

    /**
     * validate Get Hearing Request by caseRefId or caseRefId/caseStatus.
     * @param caseRef case Ref
     * @param status status
     * @return HearingRequest HearingRequest
     */
    @Override
    public GetHearingsResponse getHearings(String caseRef, String status) {
        log.debug("caseRef:{} ; status:{}", caseRef, status);
        List<CaseHearingRequestEntity> entities;
        if (!isBlank(status)) {
            entities = caseHearingRequestRepository.getHearingDetailsWithStatus(caseRef, status);
        } else {
            entities = caseHearingRequestRepository.getHearingDetails(caseRef);
        }
        return getHearingsResponseMapper.toHearingsResponse(caseRef, entities);
    }

    @Override
    public GetHearingsResponse getEmptyHearingsResponse(String caseRef) {
        return getHearingsResponseMapper.toHearingsResponse(caseRef, Lists.newArrayList());
    }

    @Override
    public ResponseEntity hearingCompletion(Long hearingId, String clientS2SToken) {
        hearingIdValidator.validateHearingId(hearingId, HEARING_ACTUALS_ID_NOT_FOUND);
        linkedHearingValidator.validateHearingActualsStatus(hearingId, HEARING_ACTUALS_INVALID_STATUS);
        hearingActualsValidator.validateHearingOutcomeInformation(hearingId);
        HearingEntity existingHearing = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        final int existingRequestVersion = existingHearing.getLatestRequestVersion();

        HearingEntity hearingEntity = updateStatus(hearingId);

        auditChangeInRequestVersion(hearingEntity, existingRequestVersion, clientS2SToken, true);
        HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity);
        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(POST_HEARING_ACTUALS_COMPLETION)
                .httpStatus(String.valueOf(HttpStatus.OK.value()))
                .source(clientS2SToken)
                .target(HMC)
                .useCurrentTimestamp(true)
                .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
        messageSenderToTopicConfiguration
            .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString(),
                         hmcHearingResponse.getHmctsServiceCode(),hearingId.toString(),
                         hearingEntity.getDeploymentId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public void sendResponse(String json, String hmctsServiceId, String deploymentId) {
        sendRspToTopic(json, hmctsServiceId, deploymentId);
    }

    @Override
    public List<DataStoreCaseDetails> getCaseSearchResults(List<String> ccdCaseRefs, String status,
                                                           String caseTypeId) {
        String elasticSearchQuery = createSearchQuery(ccdCaseRefs);
        log.info("Searching for cases with ccdCaseRefs: {}, status: {}, caseTypeId: {}",
                 elasticSearchQuery, status, caseTypeId);
        CaseSearchResult caseSearchResult =  dataStoreRepository.findAllCasesByCaseIdUsingExternalApi(caseTypeId,
                                                                                 elasticSearchQuery);
        return caseSearchResult.getCases();
    }

    private String createSearchQuery(List<String> ccdCaseRefs) {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .caseRefs(ccdCaseRefs)
            .build();
        return elasticSearchQuery.getQuery();
    }

    private void auditChangeInRequestVersion(HearingEntity hearingEntity, int existingRequestVersion,
                                             String clientS2SToken, boolean useNow) {
        int updatedRequestVersion = hearingEntity.getLatestRequestVersion();
        if (updatedRequestVersion == existingRequestVersion) {
            return;
        }

        String versionMessage = existingRequestVersion > 0
            ? String.format("requestVersion updated from <%d> to <%d>", existingRequestVersion, updatedRequestVersion)
            : String.format("requestVersion set to <%d>", updatedRequestVersion);

        try {
            JsonNode otherInfo = new ObjectMapper().readTree("{\"" + REQUEST_VERSION_UPDATE + "\":" + " \""
                                                                 + versionMessage + "\"}");
            if (existingRequestVersion > 0) {
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity)
                        .hearingEvent(REQUEST_VERSION_UPDATE)
                        .httpStatus(String.valueOf(HttpStatus.OK.value()))
                        .source(clientS2SToken)
                        .target(HMC)
                        .otherInfo(otherInfo)
                        .useCurrentTimestamp(useNow)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
            } else {
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity)
                        .hearingEvent(REQUEST_VERSION_UPDATE)
                        .httpStatus(String.valueOf(HttpStatus.OK.value()))
                        .source(clientS2SToken)
                        .target(HMC)
                        .otherInfo(otherInfo)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(hearingStatusAuditContext);
            }
        } catch (JsonProcessingException e) {
            log.error("Unable to audit requestVersion update: {}", versionMessage);
        }

    }

    private void sendRequestToHmiAndQueue(Long hearingId, String messageType, HearingRequest hearingRequest,
                                          HmiCaseDetails hmiCaseDetails,Listing listing, String deploymentId) {
        HmiSubmitHearingRequest hmiSubmitHearingRequest = hmiSubmitHearingRequestMapper
            .mapRequest(hearingRequest, hmiCaseDetails, listing);
        generatePendingRequest(hmiSubmitHearingRequest, hearingId, messageType, deploymentId);
    }

    private void validateHearingStatusForUpdate(Long hearingId) {
        String status = getStatus(hearingId);
        if (!PutHearingStatus.isValid(status) || validatePlannedStartDate(hearingId, status)) {
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

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingEntity.getLatestHearingResponse();
        return hearingResponseEntity.isPresent()
            ? hmiHearingResponseMapper.mapEntityToHmcModel(hearingResponseEntity.get(), hearingEntity)
            : hmiHearingResponseMapper.mapEntityToHmcModel(hearingEntity);
    }

    private HearingEntity updateStatus(Long hearingId) {
        ActualHearingEntity actualHearingEntity = hearingIdValidator.getActualHearing(hearingId)
            .orElseThrow(() -> new BadRequestException(HEARING_ACTUALS_MISSING_HEARING_OUTCOME));
        HearingEntity hearingEntity = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        hearingEntity.setStatus(actualHearingEntity.getHearingResultType().getLabel());
        hearingRepository.save(hearingEntity);
        return hearingEntity;
    }

    private String getNextPutHearingStatus(String currentStatus) {
        if (PutHearingStatus.HEARING_REQUESTED.name().equals(currentStatus)) {
            return PutHearingStatus.HEARING_REQUESTED.name();
        } else {
            return PutHearingStatus.UPDATE_REQUESTED.name();
        }
    }

    private HearingResponse insertHearingRequest(HearingRequest createHearingRequest,
                                                 boolean reasonableMatch,
                                                 boolean facilitiesMatch,
                                                 String deploymentId, String clientS2SToken) {
        HearingEntity savedEntity = saveHearingDetails(createHearingRequest, reasonableMatch, facilitiesMatch,
                                                       deploymentId);

        auditChangeInRequestVersion(savedEntity, 0, clientS2SToken, false);

        savePartyRelationshipDetails(createHearingRequest, savedEntity);
        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
            .hearingEntity(savedEntity)
            .hearingEvent(CREATE_HEARING_REQUEST)
            .httpStatus(String.valueOf(HttpStatus.OK.value()))
            .source(clientS2SToken)
            .target(HMC)
            .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(hearingStatusAuditContext);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity saveHearingDetails(HearingRequest createHearingRequest,
                                             boolean reasonableMatch,
                                             boolean facilitiesMatch,
                                             String deploymentId) {
        HearingEntity hearingEntity = hearingMapper
            .modelToEntity(createHearingRequest, new HearingEntity(),
                           VERSION_NUMBER_TO_INCREMENT, POST_HEARING_STATUS, reasonableMatch, facilitiesMatch,
                           deploymentId);
        return hearingRepository.save(hearingEntity);
    }

    private Listing getListing(HearingRequest hearingRequest, EntitiesMapperObject entities) {
        return listingMapper.getListing(hearingRequest.getHearingDetails(), entities.getEntities());
    }

    private HmiCaseDetails getCaseDetails(Long hearingId, HearingRequest hearingRequest) {
        Boolean isLinkedFlag = hearingRequest.getHearingDetails().getHearingIsLinkedFlag();
        int versionNumberLocal = 1;
        if ((hearingRequest instanceof UpdateHearingRequest updateHearingRequest)
            && (null != updateHearingRequest.getRequestDetails())) {
            versionNumberLocal = updateHearingRequest.getRequestDetails().getVersionNumber()
                + VERSION_NUMBER_TO_INCREMENT;
        }
        return hmiCaseDetailsMapper.getCaseDetails(
            hearingRequest.getCaseDetails(),
            versionNumberLocal,
            hearingId,
            isLinkedFlag);
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
        validateAmendReasonCodesForUpdate(hearingRequest.getHearingDetails().getAmendReasonCodes());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }
    }

    private void validateAmendReasonCodesForUpdate(List<String> amendReasonCodes) {
        if (amendReasonCodes == null || amendReasonCodes.isEmpty()) {
            throw new BadRequestException(INVALID_AMEND_REASON_CODE);
        }
    }

    private void validatePartyDetails(List<PartyDetails> partyDetails) {
        for (PartyDetails partyDetail : partyDetails) {
            if ((partyDetail.getIndividualDetails() != null && partyDetail.getOrganisationDetails() != null)
                || (partyDetail.getIndividualDetails() == null && partyDetail.getOrganisationDetails() == null)) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (PartyType.IND.getLabel().equals(partyDetail.getPartyType().toUpperCase())
                && partyDetail.getIndividualDetails() == null) {
                throw new BadRequestException(MISSING_INDIVIDUAL_DETAILS);
            } else if (PartyType.ORG.getLabel().equals(partyDetail.getPartyType())
                && partyDetail.getOrganisationDetails() == null) {
                throw new BadRequestException(MISSING_ORGANISATION_DETAILS);
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
        if (hearingDetails.getHearingWindow() != null) {
            validateHearingWindow(hearingDetails);
        }
        validateHearingChannels(hearingDetails);
    }

    private void validateHearingWindow(HearingDetails hearingDetails) {
        if (hearingDetails.getHearingWindow().getDateRangeEnd() == null
            && hearingDetails.getHearingWindow().getDateRangeStart() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(HEARING_WINDOW_EMPTY_NULL);
        }
        if ((hearingDetails.getHearingWindow().getDateRangeEnd() != null
            && hearingDetails.getHearingWindow().getDateRangeStart() != null)
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() != null) {
            throw new BadRequestException(HEARING_WINDOW_DETAILS_ARE_INVALID);
        }
        if ((hearingDetails.getHearingWindow().getDateRangeEnd() != null
            || hearingDetails.getHearingWindow().getDateRangeStart() != null)
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() != null) {
            throw new BadRequestException(HEARING_WINDOW_DETAILS_ARE_INVALID);
        }
    }

    private void validateHearingChannels(HearingDetails hearingDetails) {
        if (hearingDetails.getHearingChannels() == null) {
            throw new BadRequestException(ValidationError.HEARING_CHANNEL_EMPTY);
        }

        if (hearingDetails.getHearingChannels().isEmpty()) {
            throw new BadRequestException(ValidationError.MISSING_CHANNEL_TYPE);
        }

        Set<String> duplicateHearingChannel = new HashSet<>();
        List<String> hearingChannels = new ArrayList<>();
        for (String hearingChannel: hearingDetails.getHearingChannels()) {
            if (!duplicateHearingChannel.add(hearingChannel)) {
                hearingChannels.add(hearingChannel);
            }
        }
        if (!hearingChannels.isEmpty()) {
            throw new BadRequestException(ValidationError.NON_UNIQUE_CHANNEL_TYPE);
        }
    }

    private void validateDeleteHearingStatus(Long hearingId) {
        String status = getStatus(hearingId);
        if (!DeleteHearingStatus.isValid(status)) {
            throw new BadRequestException(INVALID_DELETE_HEARING_STATUS);
        }
    }

    public String getStatus(Long hearingId) {
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

    private void sendRspToTopic(Object response, String hmctsServiceId, String deploymentId) {
        var jsonNode = objectMapperService.convertObjectToJsonNode(response);
        messageSenderToTopicConfiguration.sendMessage(jsonNode.toString(), hmctsServiceId, NO_DEFINED, deploymentId);
    }

    private void generatePendingRequest(HmiSubmitHearingRequest hmiSubmitHearingRequest, Long hearingId,
                                        String messageType, String deploymentId) {
        var jsonNode = objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest);
        pendingRequestService.generatePendingRequest(jsonNode, hearingId, messageType, deploymentId);

    }

    private void generatePendingRequest(Long hearingId, String messageType, String deploymentId) {
        HmiDeleteHearingRequest hmiDeleteHearingRequest = hmiDeleteHearingRequestMapper.mapRequest();
        var jsonNode = objectMapperService.convertObjectToJsonNode(hmiDeleteHearingRequest);
        pendingRequestService.generatePendingRequest(jsonNode, hearingId, messageType, deploymentId);

    }
}
