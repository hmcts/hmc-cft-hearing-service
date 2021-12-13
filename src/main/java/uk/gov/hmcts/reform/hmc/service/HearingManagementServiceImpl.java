package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityCommunication;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDate;
import uk.gov.hmcts.reform.hmc.model.hmi.EntityUnavailableDay;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;
import uk.gov.hmcts.reform.hmc.model.hmi.RelatedEntity;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PHONE_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
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
public class HearingManagementServiceImpl implements HearingManagementService {

    private final DataStoreRepository dataStoreRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private HearingRepository hearingRepository;
    private final HearingMapper hearingMapper;

    private CaseHearingRequestRepository caseHearingRequestRepository;

    @Autowired
    public HearingManagementServiceImpl(RoleAssignmentService roleAssignmentService, SecurityUtils securityUtils,
                                        @Qualifier("defaultDataStoreRepository")
                                            DataStoreRepository dataStoreRepository,
                                        HearingRepository hearingRepository,
                                        HearingMapper hearingMapper,
                                        CaseHearingRequestRepository caseHearingRequestRepository) {
        this.dataStoreRepository = dataStoreRepository;
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.hearingRepository = hearingRepository;
        this.hearingMapper = hearingMapper;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
    }

    @Override
    public void getHearingRequest(Long hearingId, boolean isValid) {
        if (isValid && !hearingRepository.existsById(hearingId)) {
            throw new HearingNotFoundException(hearingId);
        }
    }

    @Override
    @Transactional
    public HearingResponse saveHearingRequest(HearingRequest hearingRequest) {
        if (hearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(hearingRequest);
        return insertHearingRequest(hearingRequest);
    }

    @Override
    public void updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest) {
        validateHearingRequest(hearingRequest);
        validateHearingId(hearingId);
        validateVersionNumber(hearingId, hearingRequest.getRequestDetails().getVersionNumber());
    }

    @Override
    public HmiCreateHearingRequest testSave(HearingRequest hearingRequest) {
        if (hearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(hearingRequest);
        return testInsertHearingRequest(hearingRequest);
    }

    private HmiCreateHearingRequest testInsertHearingRequest(HearingRequest hearingRequest) {
        HearingEntity savedEntity = saveHearingDetails(hearingRequest);
        return sendRequestToHearingManagementInterface(savedEntity, hearingRequest);
    }

    /**
     * validate Get Hearing Request by caseRefId or caseRefId/caseStatus.
     *
     * @param caseRef case Ref
     * @param status  status
     * @return HearingRequest HearingRequest
     */
    @Override
    public HearingRequest validateGetHearingsRequest(String caseRef, String status) {
        log.info("caseRef:{} ; status:{}", caseRef, status);
        // TODO: select hearing request from given caseRefId and status (if any)
        return new HearingRequest();
    }

    private HmiCreateHearingRequest sendRequestToHearingManagementInterface(HearingEntity hearingEntity,
                                                                            HearingRequest hearingRequest) {
        List<CaseClassification> caseClassifications = new ArrayList<>();
        for (CaseCategory caseCategory : hearingRequest.getCaseDetails().getCaseCategories()) {
            if (caseCategory.getCategoryType().equalsIgnoreCase("caseType")) {
                CaseClassification caseClassification = CaseClassification.builder()
                    .caseClassificationService(hearingEntity.getCaseHearingRequest().getHmctsServiceID())
                    .caseClassificationType(caseCategory.getCategoryValue())
                    .build();
                caseClassifications.add(caseClassification);
            } else if (caseCategory.getCategoryType().equalsIgnoreCase("caseSubType")) {
                CaseClassification caseClassification = CaseClassification.builder()
                    .caseClassificationService(hearingEntity.getCaseHearingRequest().getHmctsServiceID())
                    .caseClassificationSubType(caseCategory.getCategoryValue())
                    .build();
                caseClassifications.add(caseClassification);
            }
        }

        List<Entity> entities = new ArrayList<>();
        HashSet<String> preferredHearingChannels = new HashSet<>();
        if (hearingEntity.getCaseHearingRequest().getHearingParties() != null) {
            for (PartyDetails party : hearingRequest.getPartyDetails()) {
                if (party.getIndividualDetails() != null) {

                    List<EntityUnavailableDay> unavailableDays = new ArrayList<>();
                    if (party.getUnavailabilityDow() != null) {
                        for (UnavailabilityDow unavailabilityDow : party.getUnavailabilityDow()) {
                            EntityUnavailableDay entityUnavailableDay = new EntityUnavailableDay();
                            entityUnavailableDay.setUnavailableDayOfWeek(unavailabilityDow.getDow());
                            entityUnavailableDay.setUnavailableType(unavailabilityDow.getDowUnavailabilityType());
                            unavailableDays.add(entityUnavailableDay);

                        }
                    }
                    List<EntityUnavailableDate> unavailableDates = new ArrayList<>();
                    if (party.getUnavailabilityRanges() != null) {
                        for (UnavailabilityRanges unavailableDate : party.getUnavailabilityRanges()) {
                            EntityUnavailableDate entityUnavailableDates = new EntityUnavailableDate();
                            entityUnavailableDates.setUnavailableStartDate(unavailableDate.getUnavailableFromDate()
                                                                               .atStartOfDay());
                            entityUnavailableDates.setUnavailableEndDate(unavailableDate.getUnavailableToDate()
                                                                             .atStartOfDay());
                            unavailableDates.add(entityUnavailableDates);
                        }
                    }

                    List<EntityCommunication> entityCommunications = new ArrayList<>();
                    if (party.getIndividualDetails().getHearingChannelEmail() != null) {
                        EntityCommunication entityCommunication = EntityCommunication.builder()
                            .entityCommunicationDetails(party.getIndividualDetails().getHearingChannelEmail())
                            .entityCommunicationType(EMAIL_TYPE)
                            .build();
                        entityCommunications.add(entityCommunication);
                    }
                    if (party.getIndividualDetails().getHearingChannelPhone() != null) {
                        EntityCommunication entityCommunication = EntityCommunication.builder()
                            .entityCommunicationDetails(party.getIndividualDetails().getHearingChannelPhone())
                            .entityCommunicationType(PHONE_TYPE)
                            .build();
                        entityCommunications.add(entityCommunication);
                    }

                    List<RelatedEntity> relatedEntities = new ArrayList<>();
                    for (RelatedParty relatedParty : party.getIndividualDetails().getRelatedParties()) {
                        RelatedEntity relatedEntity = RelatedEntity.builder()
                            .relatedEntityId(relatedParty.getRelatedPartyID())
                            .relatedEntityRelationshipType(relatedParty.getRelationshipType())
                            .build();
                        relatedEntities.add(relatedEntity);
                    }

                    EntitySubType entitySubType = EntitySubType.builder()
                        .entityTitle(party.getIndividualDetails().getTitle())
                        .entityFirstName(party.getIndividualDetails().getFirstName())
                        .entityLastName(party.getIndividualDetails().getLastName())
                        .entityInterpreterLanguage(party.getIndividualDetails().getInterpreterLanguage())
                        .entityClassCode("IND/PERSON")
                        .entitySensitiveClient(party.getIndividualDetails().getVulnerableFlag())
                        .entityAlertMessage(party.getIndividualDetails().getVulnerabilityDetails())
                        .build();

                    Entity entity = Entity.builder()
                        .entityId(party.getPartyID())
                        .entityTypeCode(party.getPartyType())
                        .entityRoleCode(party.getPartyRole())
                        .entitySubType(entitySubType)
                        .entityHearingChannel(party.getIndividualDetails().getPreferredHearingChannel())
                        .entityCommunications(entityCommunications)
                        .entitySpecialMeasures(party.getIndividualDetails().getReasonableAdjustments())
                        .entityUnavailableDays(unavailableDays)
                        .entityUnavailableDates(unavailableDates)
                        .entityRelatedEntities(relatedEntities)
                        .build();

                    entities.add(entity);
                    preferredHearingChannels.add(party.getIndividualDetails().getPreferredHearingChannel());
                } else if (party.getOrganisationDetails() != null) {
                    EntitySubType entitySubType = EntitySubType.builder()
                        .entityCompanyName(party.getOrganisationDetails().getName())
                        .entityClassCode("ORG/ORG")
                        .build();
                    Entity entity = Entity.builder()
                        .entityId(party.getPartyID())
                        .entityTypeCode(party.getPartyType())
                        .entityRoleCode(party.getPartyRole())
                        .entitySubType(entitySubType)
                        .entityHearingChannel(party.getIndividualDetails().getPreferredHearingChannel())
                        .build();
                    entities.add(entity);
                }
            }
        }

        List<ListingJoh> listingJohs = new ArrayList<>();
        if (hearingRequest.getHearingDetails().getPanelRequirements() != null && hearingRequest.getHearingDetails()
            .getPanelRequirements().getPanelPreferences() != null) {
            for (PanelPreference panelPreference : hearingRequest.getHearingDetails().getPanelRequirements()
                .getPanelPreferences()) {
                ListingJoh listingJoh = ListingJoh.builder()
                    .listingJohId(panelPreference.getMemberID())
                    .listingJohPreference(panelPreference.getRequirementType())
                    .build();
                listingJohs.add(listingJoh);
            }
        }

        List<ListingLocation> listingLocations = new ArrayList<>();
        for (HearingLocation hearingLocation : hearingRequest.getHearingDetails().getHearingLocations()) {
            ListingLocation listingLocation = ListingLocation.builder()
                .locationId(hearingLocation.getLocationId())
                .locationType(hearingLocation.getLocationType())
                .build();
            listingLocations.add(listingLocation);
        }

        List<String> uniquePreferredHearingChannels = new ArrayList<>(preferredHearingChannels);

        Listing listing = Listing.builder()
            .listingAutoCreateFlag(hearingEntity.getCaseHearingRequest().getAutoListFlag())
            .listingPriority(hearingEntity.getCaseHearingRequest().getHearingPriorityType())
            .listingType(hearingEntity.getCaseHearingRequest().getHearingType())
            .listingDate(hearingEntity.getCaseHearingRequest().getFirstDateTimeOfHearingMustBe())
            .listingDuration(hearingEntity.getCaseHearingRequest().getRequiredDurationInMinutes())
            .listingNumberAttendees(hearingEntity.getCaseHearingRequest().getNumberOfPhysicalAttendees())
            .listingComments(hearingEntity.getCaseHearingRequest().getListingComments())
            .listingRequestedBy(hearingEntity.getCaseHearingRequest().getRequester())
            .listingPrivateFlag(hearingEntity.getCaseHearingRequest().getPrivateHearingRequiredFlag())
            .listingJohs(listingJohs)
            .listingHearingChannels(uniquePreferredHearingChannels)
            .listingLocations(listingLocations)
            .build();
        if (hearingEntity.getCaseHearingRequest().getHearingWindowStartDateRange() != null) {
            listing.setListingStartDate(hearingEntity.getCaseHearingRequest().getHearingWindowStartDateRange()
                                            .atStartOfDay());
        }
        if (hearingEntity.getCaseHearingRequest().getHearingWindowEndDateRange() != null) {
            listing.setListingEndDate(hearingEntity.getCaseHearingRequest().getHearingWindowEndDateRange()
                                            .atStartOfDay());
        }
        if (hearingRequest.getHearingDetails().getPanelRequirements().getRoleType() != null && !hearingRequest
            .getHearingDetails().getPanelRequirements().getRoleType().isEmpty()) {
            listing.setListingJohTiers(new ArrayList<>(hearingRequest.getHearingDetails().getPanelRequirements()
                                                           .getRoleType()));
        }

        HmiCaseDetails hmiCaseDetails = HmiCaseDetails.builder()
            .caseClassifications(caseClassifications)
            .caseIdHmcts(hearingEntity.getCaseHearingRequest().getCaseReference())
            .caseListingRequestId(hearingEntity.getId().toString())
            .caseJurisdiction(hearingEntity.getCaseHearingRequest().getHmctsServiceID().substring(0, 2))
            .caseTitle(hearingEntity.getCaseHearingRequest().getHmctsInternalCaseName())
            .caseCourt(hearingEntity.getCaseHearingRequest().getOwningLocationId())
            .caseRegistered(hearingEntity.getCaseHearingRequest().getCaseSlaStartDate())
            .caseInterpreterRequiredFlag(hearingEntity.getCaseHearingRequest().getInterpreterBookingRequiredFlag())
            .caseRestrictedFlag(hearingEntity.getCaseHearingRequest().getCaseRestrictedFlag())
            .build();

        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetails)
            .entities(entities)
            .listing(listing)
            .build();

        HmiCreateHearingRequest hmiCreateHearingRequest = new HmiCreateHearingRequest();
        hmiCreateHearingRequest.setHearingRequest(hmiHearingRequest);
        return hmiCreateHearingRequest;
    }

    private HearingResponse insertHearingRequest(HearingRequest hearingRequest) {
        HearingEntity savedEntity = saveHearingDetails(hearingRequest);
        sendRequestToHearingManagementInterface(savedEntity, hearingRequest);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity saveHearingDetails(HearingRequest hearingRequest) {
        HearingEntity hearingEntity = hearingMapper.modelToEntity(hearingRequest);
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

    private void validateHearingRequest(HearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
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

    private void validateHearingRequestDetails(HearingRequest hearingRequest) {
        if (hearingRequest.getRequestDetails() == null
            && hearingRequest.getHearingDetails() == null
            && hearingRequest.getCaseDetails() == null) {
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
    public void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest) {
        validateHearingId(hearingId);
        validateVersionNumber(hearingId, deleteRequest.getVersionNumber());
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

    private void validateHearingId(Long hearingId) {
        if (hearingId == null) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else {
            String hearingIdStr = String.valueOf(hearingId);
            isValidFormat(hearingIdStr);
            if (!hearingRepository.existsById(hearingId)) {
                throw new HearingNotFoundException(hearingId);
            }
        }
    }

    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
            || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }
}
