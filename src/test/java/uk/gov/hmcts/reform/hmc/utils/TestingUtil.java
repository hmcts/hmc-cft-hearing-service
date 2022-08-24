package uk.gov.hmcts.reform.hmc.utils;

import com.google.common.collect.Lists;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;
import uk.gov.hmcts.reform.hmc.data.ActualAttendeeIndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayPausesEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ActualPartyRelationshipDetailEntity;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ContactDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PartyRelationshipDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayParties;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPartyDetail;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPauseDayTime;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingActualsOutcome;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.LocationType;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.RequirementType;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_RANGE_TYPE;

public class TestingUtil {

    public static final String CASE_REFERENCE = "1111222233334444";
    public static final String INVALID_CASE_REFERENCE = "1111222233334445";
    public static final List<String> CANCELLATION_REASON_CODES = List.of("test 1", "test 2");
    public static Long ID = 2000000000L;

    private TestingUtil() {
    }

    public static RequestDetails requestDetails() {
        return new RequestDetails();
    }

    public static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(false);
        hearingDetails.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingWindow.setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(List.of("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setHearingIsLinkedFlag(Boolean.TRUE);
        hearingDetails.setHearingChannels(getHearingChannelsList());
        HearingLocation location1 = new HearingLocation();
        location1.setLocationType(LocationType.CLUSTER.getLabel());
        location1.setLocationId("Location Id");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setFacilitiesRequired(List.of("facility1", "facility2"));
        return hearingDetails;
    }

    public static PanelRequirements panelRequirements() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(List.of("RoleType1"));
        panelRequirements.setAuthorisationTypes(List.of("AuthorisationType1"));
        panelRequirements.setAuthorisationSubType(List.of("AuthorisationSubType2"));
        return panelRequirements;
    }

    public static PanelRequirementsEntity panelRequirementsEntity() {
        PanelRequirementsEntity panelRequirements = new PanelRequirementsEntity();
        panelRequirements.setRoleType("RoleType1");
        return panelRequirements;
    }

    public static PanelAuthorisationRequirementsEntity panelAuthorisationRequirementsEntity() {
        PanelAuthorisationRequirementsEntity panelAuthorisationRequirements
            = new PanelAuthorisationRequirementsEntity();
        panelAuthorisationRequirements.setAuthorisationType("AuthorisationType1");
        panelAuthorisationRequirements.setAuthorisationSubType("AuthorisationSubType2");
        return panelAuthorisationRequirements;
    }

    public static PanelSpecialismsEntity panelSpecialismsEntity() {
        PanelSpecialismsEntity panelRequirements = new PanelSpecialismsEntity();
        panelRequirements.setSpecialismType("Specialism 1");
        return panelRequirements;
    }

    public static ReasonableAdjustmentsEntity reasonableAdjustmentsEntity() {
        ReasonableAdjustmentsEntity reasonableAdjustments = new ReasonableAdjustmentsEntity();
        reasonableAdjustments.setReasonableAdjustmentCode("First reason");
        return reasonableAdjustments;
    }

    public static PanelUserRequirementsEntity panelUserRequirementsEntity() {
        PanelUserRequirementsEntity panelUserRequirements = new PanelUserRequirementsEntity();
        panelUserRequirements.setUserType("Type 1");
        panelUserRequirements.setJudicialUserId("judge1");
        panelUserRequirements.setRequirementType(RequirementType.MUSTINC);
        return panelUserRequirements;
    }

    public static RequiredFacilitiesEntity facilityEntity() {
        RequiredFacilitiesEntity requiredFacilitiesEntity = new RequiredFacilitiesEntity();
        requiredFacilitiesEntity.setFacilityType("RoleType1");
        return requiredFacilitiesEntity;
    }

    public static RequiredLocationsEntity locationEntity() {
        RequiredLocationsEntity requiredLocationsEntity = new RequiredLocationsEntity();
        requiredLocationsEntity.setLocationLevelType(LocationType.CLUSTER);
        return requiredLocationsEntity;
    }

    public static List<UnavailabilityEntity> unavailabilityEntity() {
        List<UnavailabilityEntity> unavailabilityEntities = new ArrayList<>();
        setUnAvailabilityValues(unavailabilityEntities, UNAVAILABILITY_DOW_TYPE);
        setUnAvailabilityValues(unavailabilityEntities, UNAVAILABILITY_RANGE_TYPE);
        return unavailabilityEntities;
    }

    private static void setUnAvailabilityValues(List<UnavailabilityEntity> unavailabilityEntities,
                                                String unavailabilityType) {
        LocalDate startDate = LocalDate.of(2020, 12, 20);
        LocalDate endDate = LocalDate.of(2020, 12, 20);
        UnavailabilityEntity unavailabilityEntity = new UnavailabilityEntity();
        unavailabilityEntity.setDayOfWeekUnavailable(DayOfWeekUnavailable.FRIDAY);
        unavailabilityEntity.setEndDate(endDate);
        unavailabilityEntity.setDayOfWeekUnavailableType(DayOfWeekUnAvailableType.ALL);
        unavailabilityEntity.setStartDate(startDate);
        unavailabilityEntity.setUnAvailabilityType(unavailabilityType);
        unavailabilityEntities.add(unavailabilityEntity);
    }

    public static OrganisationDetailEntity organisationDetailEntity() {
        OrganisationDetailEntity organisationDetailEntity = new OrganisationDetailEntity();
        organisationDetailEntity.setOrganisationName("name");
        organisationDetailEntity.setOrganisationTypeCode("code");
        organisationDetailEntity.setHmctsOrganisationReference("reference");
        return organisationDetailEntity;
    }

    public static IndividualDetailEntity individualDetailEntity() {
        IndividualDetailEntity individualDetailEntity = new IndividualDetailEntity();
        individualDetailEntity.setTitle("mr");
        individualDetailEntity.setFirstName("joe");
        individualDetailEntity.setLastName("bloggs");
        individualDetailEntity.setChannelType("channelType");
        individualDetailEntity.setInterpreterLanguage("english");
        individualDetailEntity.setVulnerableFlag(true);
        individualDetailEntity.setVulnerabilityDetails("details");
        individualDetailEntity.setCustodyStatus("custodyStatus");
        individualDetailEntity.setOtherReasonableAdjustmentDetails("otherReason");
        return individualDetailEntity;
    }

    public static List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType(PartyType.IND.getLabel());
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType(PartyType.IND.getLabel());
        partyDetails2.setPartyRole("DEF2");

        return List.of(partyDetails1, partyDetails2);
    }

    public static List<PartyDetails> partyDetailsWithOrgType() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType(PartyType.ORG.getLabel());
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType(PartyType.ORG.getLabel());
        partyDetails2.setPartyRole("DEF2");

        return List.of(partyDetails1, partyDetails2);
    }

    public static List<CaseCategoriesEntity> caseCategoriesEntities() {
        CaseCategoriesEntity caseCategoriesEntity = new CaseCategoriesEntity();
        caseCategoriesEntity.setCategoryType(CaseCategoryType.CASETYPE);
        caseCategoriesEntity.setCaseCategoryValue("PROBATE");
        return List.of(caseCategoriesEntity);
    }

    public static List<HearingChannelsEntity> hearingChannelsEntity() {
        HearingChannelsEntity hce1 = new HearingChannelsEntity();
        hce1.setHearingChannelType("someChannelType");
        HearingChannelsEntity hce2 = new HearingChannelsEntity();
        hce2.setHearingChannelType("someOtherChannelType");
        return List.of(hce1,hce2);
    }

    public static List<String> getHearingChannelsList() {
        return List.of("someChannelType", "someOtherChannelType");
    }

    public static IndividualDetails individualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        individualDetails.setCustodyStatus("custodyStatus");
        individualDetails.setOtherReasonableAdjustmentDetails("otherReason");
        List<RelatedParty> relatedParties = new ArrayList<>();
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("P1");
        relatedParty1.setRelationshipType("R1");
        relatedParties.add(relatedParty1);
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("P2");
        relatedParty2.setRelationshipType("R2");
        relatedParties.add(relatedParty2);
        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    public static IndividualDetails relatedPartyMandatoryFieldMissing() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        List<RelatedParty> relatedParties = new ArrayList<>();
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParties.add(relatedParty1);
        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    public static IndividualDetails individualWithoutRelatedPartyDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        return individualDetails;
    }

    public static OrganisationDetails organisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("type");
        organisationDetails.setCftOrganisationID("cft");
        return organisationDetails;
    }

    public static OrganisationDetails organisationDetailsIdNull() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("type");
        organisationDetails.setCftOrganisationID(null);
        return organisationDetails;
    }

    public static HearingEntity hearingEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setStatus(POST_HEARING_STATUS);
        hearingEntity.setLinkedOrder(1L);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        caseHearingRequestEntity.setHearingParties(List.of(new HearingPartyEntity()));
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));
        return hearingEntity;
    }

    public static HearingEntity hearingEntityWithLinkDetails() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setStatus(POST_HEARING_STATUS);
        hearingEntity.setLinkedOrder(1L);
        LinkedGroupDetails linkedGroupDetailsEntity = linkedGroupDetailsEntity();
        hearingEntity.setLinkedGroupDetails(linkedGroupDetailsEntity);
        hearingEntity.setLinkedOrder(1L);
        hearingEntity.setIsLinkedFlag(Boolean.TRUE);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));
        return hearingEntity;
    }

    public static LinkedGroupDetails linkedGroupDetailsEntity() {
        LinkedGroupDetails entity = new LinkedGroupDetails();
        entity.setLinkedGroupId(1L);
        entity.setRequestName("RequestName");
        entity.setReasonForLink("ReasonForLink");
        entity.setLinkType(LinkType.ORDERED);
        entity.setLinkedComments("linkComments");
        entity.setStatus("PENDING");
        entity.setRequestDateTime(LocalDateTime.now());
        entity.setLinkedGroupLatestVersion(1L);
        return entity;
    }

    public static CaseHearingRequestEntity caseHearingRequestEntity() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        entity.setAutoListFlag(false);
        entity.setHearingType("Some hearing type");
        entity.setRequiredDurationInMinutes(10);
        entity.setHearingPriorityType("Priority type");
        entity.setHmctsServiceCode("ABA1");
        entity.setCaseReference("1111222233334444");
        entity.setCaseUrlContextPath("https://www.google.com");
        entity.setHmctsInternalCaseName("Internal case name");
        entity.setOwningLocationId("CMLC123");
        entity.setCaseRestrictedFlag(true);
        entity.setCaseSlaStartDate(LocalDate.parse("2020-08-10"));
        entity.setVersionNumber(1);
        entity.setHearingRequestReceivedDateTime(LocalDateTime.parse("2020-08-10T12:20:00"));
        return entity;

    }

    public static IndividualDetails individualContactDetails_HearingChannelEmail() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail(List.of("hearing.channel@email.com"));
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails_HearingChannelPhone() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelPhone(List.of("01234567890"));
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail(List.of("hearing.channel@email.com"));
        individualDetails.setHearingChannelPhone(List.of("01234567890"));
        return individualDetails;

    }

    public static ContactDetailsEntity contactDetailsEntity_Email() {
        ContactDetailsEntity contactDetailsEntity = new ContactDetailsEntity();
        contactDetailsEntity.setContactDetails("hearing.channel@email.com");
        contactDetailsEntity.setContactType("email");
        return contactDetailsEntity;
    }

    public static ContactDetailsEntity contactDetailsEntity_Phone() {
        ContactDetailsEntity contactDetailsEntity = new ContactDetailsEntity();
        contactDetailsEntity.setContactDetails("01234567890");
        contactDetailsEntity.setContactType("phone");
        return contactDetailsEntity;
    }

    public static DeleteHearingRequest deleteHearingRequest() {
        DeleteHearingRequest request = new DeleteHearingRequest();
        request.setCancellationReasonCodes(CANCELLATION_REASON_CODES);
        return request;
    }

    public static UpdateHearingRequest updateHearingRequest() {
        return updateHearingRequest(1);
    }

    public static UpdateHearingRequest updateHearingRequest(int version) {
        UpdateHearingRequest request = new UpdateHearingRequest();
        HearingDetails hearingDetails = hearingDetails();
        hearingDetails.setAmendReasonCodes(List.of("reason"));
        request.setHearingDetails(hearingDetails);
        request.setCaseDetails(caseDetails());
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(version);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static UpdateHearingRequest updateHearingRequestWithCaseSubType(int version) {
        UpdateHearingRequest request = new UpdateHearingRequest();
        HearingDetails hearingDetails = hearingDetails();
        hearingDetails.setAmendReasonCodes(List.of("reason"));
        request.setHearingDetails(hearingDetails);
        request.setCaseDetails(caseDetailsWithCaseSubType());
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(version);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static UpdateHearingRequest validUpdateHearingRequest() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        HearingDetails hearingDetails = hearingDetails();
        hearingDetails.setAmendReasonCodes(List.of("reason 1", "reason 2"));
        request.setHearingDetails(hearingDetails);
        CaseDetails caseDetails = getValidCaseDetails();
        request.setCaseDetails(caseDetails);
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(1);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static Entity getEntity(List<String> reasonableAdjustment) {
        return Entity.builder()
            .entityId("entityId")
            .entityOtherConsiderations(reasonableAdjustment)
            .build();
    }

    public static Optional<RoomAttribute> getRoomAttribute(String roomAttributeCode,
                                                           String roomAttributeName,
                                                           String reasonableAdjustmentCode,
                                                           Boolean facility) {
        RoomAttribute roomAttribute = RoomAttribute.builder()
            .roomAttributeCode(roomAttributeCode)
            .roomAttributeName(roomAttributeName)
            .reasonableAdjustmentCode(reasonableAdjustmentCode)
            .facility(facility)
            .build();
        return Optional.of(roomAttribute);
    }

    public static CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef(CASE_REFERENCE);
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("CASETYPE");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    public static CaseDetails caseDetailsWithCaseSubType() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef(CASE_REFERENCE);
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("CASETYPE");
        category.setCategoryValue("PROBATE");
        CaseCategory categorySubType = new CaseCategory();
        categorySubType.setCategoryType("CASESUBTYPE");
        categorySubType.setCategoryValue("PROBATE");
        categorySubType.setCategoryParent("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseCategories.add(categorySubType);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    public static CaseDetails getValidCaseDetails() {

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef(CASE_REFERENCE);
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");

        CaseCategory categorySubType = new CaseCategory();
        categorySubType.setCategoryType("caseSubType");
        categorySubType.setCategoryValue("PROBATE");
        categorySubType.setCategoryParent("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseCategories.add(categorySubType);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    public static HearingResponse deleteHearingResponse() {
        HearingResponse response = new HearingResponse();
        response.setHearingRequestId(1L);
        response.setTimeStamp(LocalDateTime.now());
        response.setStatus(CANCELLATION_REQUESTED);
        response.setVersionNumber(1);
        return response;
    }

    public static GetHearingsResponse getHearingsResponseWhenDataIsPresent(String caseRef, String status) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setHmctsServiceCode("AB1A");
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(ID);
        caseHearing.setHearingRequestDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseHearing.setHearingType("45YAO6VflHAmYy7N85fv");
        caseHearing.setHmcStatus(status);
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.parse("2020-08-10T12:20:00"));
        caseHearing.setListAssistCaseStatus("EXCEPTION");
        caseHearing.setHearingListingStatus("listingStatus");
        caseHearing.setHearingIsLinkedFlag(Boolean.TRUE);
        HearingDaySchedule schedule = new HearingDaySchedule();
        schedule.setHearingStartDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        schedule.setHearingEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        schedule.setListAssistSessionId("jvjyVv8aecmpBgo3RnGb");
        schedule.setHearingVenueId("venue");
        schedule.setHearingRoomId("room1");
        schedule.setHearingJudgeId("judge1");
        Attendee attendee = new Attendee();
        attendee.setPartyId("partyId1");
        attendee.setHearingSubChannel("subChannel1");
        List<Attendee> attendeeList = new ArrayList<>();
        attendeeList.add(attendee);
        schedule.setAttendees(attendeeList);
        caseHearing.setHearingDaySchedule(List.of(schedule));
        caseHearing.setHearingChannels(getHearingChannelsList());
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);
        getHearingsResponse.setCaseHearings(caseHearingList);
        return getHearingsResponse;
    }

    public static CaseHearingRequestEntity getCaseHearingsEntities() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        hearingEntity.setIsLinkedFlag(Boolean.TRUE);
        entity.setCaseHearingID(ID);
        hearingEntity.setStatus("HEARING_REQUESTED");
        entity.setVersionNumber(1);
        hearingEntity.setIsLinkedFlag(true);
        hearingEntity.setLinkedGroupDetails(getLinkedGroupDetails());
        entity.setHearing(hearingEntity);
        entity.setHmctsServiceCode("ABA1");
        entity.setCaseReference("12345");
        entity.setHearingType("Some hearing type");
        entity.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        entity.setHearingChannels(hearingChannelsEntity());
        return entity;
    }

    public static CaseHearingRequestEntity getCaseHearingsEntities(String status) {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setIsLinkedFlag(Boolean.TRUE);
        entity.setCaseHearingID(2000000000L);
        hearingEntity.setStatus(status);
        entity.setVersionNumber(1);
        hearingEntity.setIsLinkedFlag(true);
        hearingEntity.setLinkedGroupDetails(getLinkedGroupDetails());
        entity.setHearing(hearingEntity);
        entity.setHmctsServiceCode("ABA1");
        entity.setCaseReference("12345");
        entity.setHearingType("Some hearing type");
        entity.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        return entity;
    }

    public static LinkedGroupDetails getLinkedGroupDetails() {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(1L);
        linkedGroupDetails.setRequestId("requestId");
        linkedGroupDetails.setLinkedComments("linkComments");
        linkedGroupDetails.setRequestName("requestName");
        return linkedGroupDetails;
    }

    public static List<NonStandardDurationsEntity> getNonStandardDurationEntities() {
        NonStandardDurationsEntity nonStandardDurationsEntity = new NonStandardDurationsEntity();
        nonStandardDurationsEntity.setNonStandardHearingDurationReasonType("Reason");
        return List.of(nonStandardDurationsEntity);
    }

    public static List<CaseHearingRequestEntity> getCaseHearingsEntitiesWithStatus() {
        List<CaseHearingRequestEntity> entities = new ArrayList<>();
        getFirstEntity(entities);
        getSecondEntity(entities);
        return entities;
    }

    private static void getFirstEntity(List<CaseHearingRequestEntity> entities) {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        entity1.setCaseHearingID(ID);
        hearingEntity.setStatus("HEARING_REQUESTED");
        entity1.setHearing(hearingEntity);
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        entities.add(entity1);
    }

    public static HearingActual getHearingActual(HearingActualsOutcome hearingActualsOutcome,
                                                 List<ActualHearingDay> actualHearingDays) {
        HearingActual hearingActual = new HearingActual();
        hearingActual.setActualHearingDays(actualHearingDays);
        hearingActual.setHearingOutcome(hearingActualsOutcome);
        return hearingActual;
    }

    public static HearingActualsOutcome getHearingActualOutcome(String hearingType, Boolean hearingFinal,
                                                                String hearingResult, String hearingReason,
                                                                LocalDate hearingDate) {
        HearingActualsOutcome hearingActualsOutcome = new HearingActualsOutcome();
        hearingActualsOutcome.setHearingType(hearingType);
        hearingActualsOutcome.setHearingFinalFlag(hearingFinal);
        hearingActualsOutcome.setHearingResult(hearingResult);
        hearingActualsOutcome.setHearingResultReasonType(hearingReason);
        hearingActualsOutcome.setHearingResultDate(hearingDate);
        return hearingActualsOutcome;
    }

    public static ActualHearingDay getHearingActualDay(LocalDate hearingDate, LocalDateTime startTime,
                                                       LocalDateTime endTime,
                                                       List<ActualHearingDayPauseDayTime> pauseDateTimes,
                                                       List<ActualHearingDayParties> actualDayParties) {
        ActualHearingDay hearingActualDay = new ActualHearingDay();
        hearingActualDay.setHearingDate(hearingDate);
        hearingActualDay.setHearingStartTime(startTime);
        hearingActualDay.setHearingEndTime(endTime);
        hearingActualDay.setActualDayParties(actualDayParties);
        hearingActualDay.setPauseDateTimes(pauseDateTimes);
        return hearingActualDay;
    }

    public static ActualHearingDayPauseDayTime getHearingActualDayPause(LocalDateTime startTime,
                                                                        LocalDateTime endTime) {
        ActualHearingDayPauseDayTime actualHearingDayPauseDayTime = new ActualHearingDayPauseDayTime();
        actualHearingDayPauseDayTime.setPauseEndTime(endTime);
        actualHearingDayPauseDayTime.setPauseStartTime(startTime);
        return actualHearingDayPauseDayTime;
    }

    public static ActualHearingDayParties getHearingActualDayParties(
        String partyId, String partyRole, ActualHearingDayPartyDetail individualDetails,
        String actualOrganisationName, String partyChannelSubType, Boolean didNotAttendFlag,
        String representedParty) {
        ActualHearingDayParties actualHearingDayParties = new ActualHearingDayParties();
        actualHearingDayParties.setActualPartyId(partyId);
        actualHearingDayParties.setPartyRole(partyRole);
        actualHearingDayParties.setIndividualDetails(individualDetails);
        actualHearingDayParties.setActualOrganisationName(actualOrganisationName);
        actualHearingDayParties.setPartyChannelSubType(partyChannelSubType);
        actualHearingDayParties.setDidNotAttendFlag(didNotAttendFlag);
        actualHearingDayParties.setRepresentedParty(representedParty);
        return actualHearingDayParties;
    }

    private static void getSecondEntity(List<CaseHearingRequestEntity> entities) {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000001L);
        entity1.setCaseHearingID(2000000001L);
        hearingEntity.setStatus("HEARING_UPDATED");
        entity1.setHearing(hearingEntity);
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("4567");
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        entities.add(entity1);
    }

    public static HearingResponseEntity hearingResponseEntities() {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setRequestVersion(1);
        entity.setRequestTimeStamp(LocalDateTime.parse("2020-08-10T12:20:00"));
        entity.setHearingResponseId(2L);
        entity.setListingStatus("listingStatus");
        entity.setListingCaseStatus("Case_listingStatus");
        entity.setCancellationReasonType("Cancelled Reason 1");
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntities() {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(LocalDateTime.parse("2020-08-10T12:20:00"));
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setVenueId("venue1");
        entity.setRoomId("room1");
        entity.setHearingAttendeeDetails(List.of(hearingAttendeeDetailsEntity()));
        entity.setHearingDayPanel(List.of(hearingDayPanelEntities()));
        return entity;
    }

    public static HearingDayPanelEntity hearingDayPanelEntities() {
        HearingDayPanelEntity entity = new HearingDayPanelEntity();
        entity.setPanelUserId("PanelUser1");
        entity.setIsPresiding(false);
        return entity;
    }

    public static HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity() {
        HearingAttendeeDetailsEntity entity = new HearingAttendeeDetailsEntity();
        entity.setPartyId("Party1");
        entity.setPartySubChannelType("SubChannel1");
        return entity;
    }

    public static GetHearingsResponse getHearingsResponseWhenNoData(String caseRef) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setCaseHearings(new ArrayList<>());
        return getHearingsResponse;
    }

    public static HearingEntity getCaseHearingsEntity(PartyType partyType) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(List.of(hearingResponseEntity()));
        if (partyType.getLabel() == PartyType.IND.getLabel()) {
            hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntityWithPartyOrg()));
        } else {
            hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntityWithPartyInd()));
        }
        hearingEntity.getCaseHearingRequests().get(0).setVersionNumber(1);
        return hearingEntity;
    }

    public static HearingEntity getCaseHearingsEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(List.of(hearingResponseEntity()));
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntityWithPartyOrg()));

        hearingEntity.getCaseHearingRequests().get(0).setVersionNumber(1);
        hearingEntity.getCaseHearingRequests().get(0).setHearingParties(List.of(
            hearingPartyEntitySetReference("reference")));
        return hearingEntity;
    }

    public static HearingEntity getCaseHearingsEntity(String status) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus(status);
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntityWithPartyOrg()));

        hearingEntity.getCaseHearingRequests().get(0).setVersionNumber(1);
        hearingEntity.getCaseHearingRequests().get(0).setHearingParties(Arrays.asList(hearingPartyEntityOrg()));
        return hearingEntity;
    }

    public static HearingEntity getHearingsEntityForHearingActuals(String status) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        hearingEntity.setStatus(status);
        hearingEntity.setHearingResponses(List.of(hearingResponseEntity()));
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntityWithPartyOrg());

        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        caseHearingRequestEntity.setCaseHearingID(ID);
        caseHearingRequestEntity.setVersionNumber(1);
        caseHearingRequestEntity.setHearingType("hearingType");
        caseHearingRequestEntity.setHmctsServiceCode("serviceCode");
        caseHearingRequestEntity.setCaseReference("caseRef");
        caseHearingRequestEntity.setExternalCaseReference("extCaseRef");
        caseHearingRequestEntity.setCaseUrlContextPath("contextPath");
        caseHearingRequestEntity.setHmctsInternalCaseName("caseName");
        caseHearingRequestEntity.setPublicCaseName("publicCaseName");
        caseHearingRequestEntity.setAdditionalSecurityRequiredFlag(true);
        caseHearingRequestEntity.setInterpreterBookingRequiredFlag(true);
        caseHearingRequestEntity.setOwningLocationId("locationId");
        caseHearingRequestEntity.setCaseRestrictedFlag(true);
        caseHearingRequestEntity.setCaseSlaStartDate(LocalDate.of(2000, 01, 01));
        caseHearingRequestEntity.setHearingParties(List.of(
            hearingPartyEntitySetReference("reference2"),
            hearingPartyEntitySetReference("reference"),
            hearingPartyEntitySetReference("reference2")
        ));
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities());

        hearingEntity.getHearingResponses().get(0).setActualHearingEntity(actualHearingEntity(PartyType.ORG));
        return hearingEntity;
    }

    public static HearingEntity getHearingsEntityForHearingActualsIndividual() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(ID);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(List.of(hearingResponseEntity()));
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntityWithPartyInd());

        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        caseHearingRequestEntity.setVersionNumber(1);
        caseHearingRequestEntity.setCaseHearingID(ID);
        caseHearingRequestEntity.setHearingType("hearingType");
        caseHearingRequestEntity.setHmctsServiceCode("serviceCode");
        caseHearingRequestEntity.setCaseReference("caseRef");
        caseHearingRequestEntity.setExternalCaseReference("extCaseRef");
        caseHearingRequestEntity.setCaseUrlContextPath("contextPath");
        caseHearingRequestEntity.setHmctsInternalCaseName("caseName");
        caseHearingRequestEntity.setPublicCaseName("publicCaseName");
        caseHearingRequestEntity.setAdditionalSecurityRequiredFlag(true);
        caseHearingRequestEntity.setInterpreterBookingRequiredFlag(true);
        caseHearingRequestEntity.setOwningLocationId("locationId");
        caseHearingRequestEntity.setCaseRestrictedFlag(true);
        caseHearingRequestEntity.setCaseSlaStartDate(LocalDate.of(2000, 01, 01));
        caseHearingRequestEntity.setHearingParties(List.of(hearingPartyEntityInd()));
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities());

        hearingEntity.getHearingResponses().get(0).setActualHearingEntity(actualHearingEntity(PartyType.IND));
        return hearingEntity;
    }

    public static ActualHearingEntity actualHearingEntity(PartyType partyType) {
        ActualHearingEntity entity = new ActualHearingEntity();
        entity.setActualHearingType("hearingType");
        entity.setHearingResultType(HearingResultType.ADJOURNED);
        entity.setHearingResultReasonType("resultReason");
        entity.setHearingResultDate(LocalDate.of(2000, 01, 01));
        entity.setActualHearingDay(List.of(actualHearingDayEntity(partyType)));
        entity.setActualHearingIsFinalFlag(true);
        return entity;
    }

    public static ActualPartyRelationshipDetailEntity actualPartyRelationshipDetailEntity(
        ActualHearingPartyEntity actualHearingPartyEntity) {
        ActualPartyRelationshipDetailEntity entity = ActualPartyRelationshipDetailEntity
            .builder()
            .actualPartyRelationshipId(1L)
            .targetActualParty(actualHearingPartyEntity)
            .sourceActualParty(actualHearingPartyEntity).build();
        return entity;
    }

    public static ActualHearingDayEntity actualHearingDayEntity(PartyType partyType) {
        ActualHearingDayEntity entity = new ActualHearingDayEntity();
        entity.setHearingDate(LocalDate.of(2000, 01, 01));
        entity.setStartDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setActualHearingDayPauses(List.of(actualHearingDayPausesEntity()));
        entity.setActualHearingParty(List.of(actualHearingPartyEntity(partyType)));
        return entity;
    }

    public static ActualHearingDayPausesEntity actualHearingDayPausesEntity() {
        ActualHearingDayPausesEntity entity = new ActualHearingDayPausesEntity();
        entity.setPauseDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setResumeDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        return entity;
    }

    public static ActualHearingPartyEntity actualHearingPartyEntity(PartyType partyType) {
        ActualHearingPartyEntity entity = new ActualHearingPartyEntity();
        entity.setActualPartyId(1L);
        entity.setPartyId("1");
        entity.setActualPartyRoleType("roleType");
        entity.setDidNotAttendFlag(false);
        entity.setActualAttendeeIndividualDetail(actualAttendeeIndividualDetailEntity(partyType));
        entity.setActualPartyRelationshipDetail(List.of(actualPartyRelationshipDetailEntity(entity)));
        return entity;
    }

    public static ActualAttendeeIndividualDetailEntity actualAttendeeIndividualDetailEntity(PartyType partyType) {
        ActualAttendeeIndividualDetailEntity entity = new ActualAttendeeIndividualDetailEntity();
        if (PartyType.IND.equals(partyType)) {
            entity.setFirstName("firstName");
            entity.setLastName("lastName");
        } else {
            entity.setPartyOrganisationName("partyOrgName");
        }
        entity.setPartyActualSubChannelType("partySubChannel");
        return entity;
    }

    public static HearingResponseEntity hearingResponseEntity() {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setRequestVersion(1);
        entity.setRequestTimeStamp(LocalDateTime.parse("2020-08-10T12:20:00"));
        entity.setHearingResponseId(2L);
        entity.setRequestVersion(10);
        entity.setListingStatus("Fixed");
        entity.setListingCaseStatus(ListAssistCaseStatus.CASE_CREATED.name());
        entity.setCancellationReasonType("Cancelled Reason 1");
        entity.setHearingDayDetails(List.of(hearingDayDetailsEntity()));
        return entity;
    }

    public static HearingResponseEntity hearingResponseEntity(Integer version, Integer requestVersion,
                                                              LocalDateTime requestTimestamp,
                                                              List<HearingDayDetailsEntity> hearingDayDetailsEntities) {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setRequestVersion(requestVersion);
        entity.setRequestTimeStamp(requestTimestamp);
        entity.setHearingResponseId(2L);
        entity.setListingStatus("Fixed");
        entity.setListingCaseStatus(ListAssistCaseStatus.CASE_CREATED.name());
        entity.setCancellationReasonType("Cancelled Reason 1");
        entity.setHearingDayDetails(hearingDayDetailsEntities);
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntity() {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(LocalDateTime.of(2000, 8, 10, 12, 20));
        entity.setEndDateTime(LocalDateTime.of(2000, 8, 10, 12, 20));
        entity.setRoomId("roomId");
        entity.setVenueId("venueId");

        HearingAttendeeDetailsEntity attendee = new HearingAttendeeDetailsEntity();
        attendee.setId(ID);
        attendee.setPartySubChannelType("partySubChannelA");
        attendee.setPartyId("reference");

        HearingAttendeeDetailsEntity attendee2 = new HearingAttendeeDetailsEntity();
        attendee2.setId(ID);
        attendee2.setPartySubChannelType("partySubChannelB");
        attendee2.setPartyId("reference2");


        HearingAttendeeDetailsEntity attendee3 = new HearingAttendeeDetailsEntity();
        attendee3.setId(ID);
        attendee3.setPartySubChannelType("partySubChannelC");
        attendee3.setPartyId("reference3");
        entity.setHearingAttendeeDetails(List.of(attendee, attendee2, attendee3));
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntity(LocalDateTime startDateTime) {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(startDateTime);
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setVenueId("venue1");
        entity.setRoomId("room1");
        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityOrg() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.ORG);
        entity.setPartyRoleType("role");
        entity.setUnavailabilityEntity(unavailabilityEntity());
        entity.setOrganisationDetailEntity(organisationDetailEntity());

        return entity;
    }


    public static HearingPartyEntity hearingPartyEntityForClone() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.ORG);
        entity.setPartyRoleType("role");
        entity.setUnavailabilityEntity(unavailabilityEntity());
        entity.setContactDetailsEntity(List.of(contactDetailsEntity_Email()));
        entity.setOrganisationDetailEntity(organisationDetailEntity());
        entity.setReasonableAdjustmentsEntity(List.of(reasonableAdjustmentsEntity()));
        entity.setPartyRelationshipDetailsEntity(List.of(
            partyRelationshipDetailsEntity(
                "P1", "A"),
            partyRelationshipDetailsEntity(
                "P2", "B")
        ));

        return entity;
    }

    public static HearingPartyEntity hearingPartyEntitySetReference(String partyReference) {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference(partyReference);
        entity.setPartyType(PartyType.ORG);
        entity.setPartyRoleType("role");
        entity.setUnavailabilityEntity(unavailabilityEntity());
        entity.setOrganisationDetailEntity(organisationDetailEntity());
        entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());

        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityInd() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.IND);
        entity.setPartyRoleType("role");
        entity.setContactDetailsEntity(List.of(contactDetailsEntity_Email(), contactDetailsEntity_Phone()));
        entity.setIndividualDetailEntity(individualDetailEntity());
        entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
        entity.setReasonableAdjustmentsEntity(List.of(reasonableAdjustmentsEntity()));
        entity.setPartyRelationshipDetailsEntity(List.of(
            partyRelationshipDetailsEntity("P1", "A"),
            partyRelationshipDetailsEntity("P2", "B")
        ));
        return entity;
    }

    public static PartyRelationshipDetailsEntity partyRelationshipDetailsEntity(String targetTechPartyId,
                                                                                 String relationshipType) {

        HearingPartyEntity targetHearingPartyEntity = new HearingPartyEntity();
        targetHearingPartyEntity.setPartyReference(targetTechPartyId);

        return PartyRelationshipDetailsEntity.builder()
            .targetTechParty(targetHearingPartyEntity)
            .relationshipType(relationshipType)
            .build();
    }

    public static CaseHearingRequestEntity caseHearingRequestEntityWithPartyOrg() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setVersionNumber(1);
        entity1.setCaseHearingID(ID);
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        entity1.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        entity1.setHearingParties(List.of(hearingPartyEntityOrg()));
        entity1.setHearingChannels(hearingChannelsEntity());
        return entity1;
    }

    public static CaseHearingRequestEntity caseHearingRequestEntityWithPartyOrgForClone() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setVersionNumber(1);
        entity1.setCaseHearingID(ID);
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        entity1.setHearingParties(List.of(hearingPartyEntityOrg()));
        entity1.setCaseCategories(caseCategoriesEntities());
        entity1.setNonStandardDurations(getNonStandardDurationEntities());
        entity1.setRequiredFacilities(List.of(requiredFacilitiesEntity()));
        entity1.setRequiredLocations(List.of(requiredLocationsEntity()));
        entity1.setPanelRequirements(List.of(panelRequirementsEntity()));
        entity1.setPanelAuthorisationRequirements(List.of(panelAuthorisationRequirementsEntity()));
        entity1.setPanelUserRequirements(List.of(panelUserRequirementsEntity()));
        entity1.setPanelSpecialisms(List.of(panelSpecialismsEntity()));
        return entity1;
    }

    private static RequiredFacilitiesEntity requiredFacilitiesEntity() {
        RequiredFacilitiesEntity entity = new RequiredFacilitiesEntity();
        entity.setFacilityType("string");
        return entity;
    }

    private static RequiredLocationsEntity requiredLocationsEntity() {
        RequiredLocationsEntity entity = new RequiredLocationsEntity();
        entity.setLocationLevelType(LocationType.COURT);
        return entity;
    }

    private static CaseHearingRequestEntity caseHearingRequestEntityWithPartyInd() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setVersionNumber(1);
        entity1.setCaseHearingID(ID);

        entity1.setHearing(getCaseHearingsEntity());
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        entity1.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        entity1.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        entity1.setHearingParties(List.of(hearingPartyEntityInd()));
        entity1.setHearingChannels(hearingChannelsEntity());
        return entity1;
    }

    public static UpdateHearingRequest updateHearingRequestWithPartyDetails(boolean isCftOrganisationIdNull) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(1);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setRequestDetails(requestDetails);
        request.setHearingDetails(hearingDetailsWithAllFields());
        request.setCaseDetails(getValidCaseDetails());
        request.setPartyDetails(partyDetailsWith2Parties(isCftOrganisationIdNull));

        return request;
    }


    private static List<PartyDetails> partyDetailsWith2Parties(boolean isCftOrganisationIdNull) {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType(PartyType.IND.getLabel());
        partyDetails1.setPartyRole("DEF");
        partyDetails1.setIndividualDetails(allIndividualDetails());
        partyDetails1.setUnavailabilityDow(unavailabilityDowDetails());
        partyDetails1.setUnavailabilityRanges(unAvailabilityRanges());

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType(PartyType.ORG.getLabel());
        partyDetails2.setPartyRole("DEF2");
        if (isCftOrganisationIdNull) {
            partyDetails2.setOrganisationDetails(organisationDetailsIdNull());
        } else {
            partyDetails2.setOrganisationDetails(organisationDetails());
        }

        List<PartyDetails> partyDetails = List.of(partyDetails1, partyDetails2);
        return partyDetails;
    }

    private static List<UnavailabilityRanges> unAvailabilityRanges() {
        UnavailabilityRanges detail1 = new UnavailabilityRanges();
        detail1.setUnavailableFromDate(LocalDate.parse("2021-10-20"));
        detail1.setUnavailableToDate(LocalDate.parse("2021-12-31"));
        detail1.setUnavailabilityType("All Day");
        UnavailabilityRanges detail2 = new UnavailabilityRanges();
        detail2.setUnavailableFromDate(LocalDate.parse("2030-10-20"));
        detail2.setUnavailableToDate(LocalDate.parse("2030-10-22"));
        detail2.setUnavailabilityType("All Day");
        List<UnavailabilityRanges> unavailabilityRanges = List.of(detail1, detail2);
        return unavailabilityRanges;
    }

    private static List<UnavailabilityDow> unavailabilityDowDetails() {
        UnavailabilityDow detail1 = new UnavailabilityDow();
        detail1.setDowUnavailabilityType("PM");
        detail1.setDow("Monday");
        UnavailabilityDow detail2 = new UnavailabilityDow();
        detail2.setDowUnavailabilityType("All Day");
        detail2.setDow("Thursday");
        List<UnavailabilityDow> unavailabilityDows = Lists.newArrayList(detail1, detail2);
        return unavailabilityDows;
    }

    public static HearingDetails hearingDetailsWithAllFields() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(false);
        hearingDetails.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        hearingDetails.setAmendReasonCodes(List.of("reason 1", "reason 2"));
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingWindow.setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(List.of("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setNumberOfPhysicalAttendees(4);
        hearingDetails.setHearingInWelshFlag(false);
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("COURT");
        location1.setLocationType("Court");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setFacilitiesRequired(List.of("facility1", "facility2"));
        hearingDetails.setListingComments("Some listing comments");
        hearingDetails.setHearingRequester("Some judge");
        hearingDetails.setPrivateHearingRequiredFlag(false);
        hearingDetails.setLeadJudgeContractType("AB123");
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        hearingDetails.getPanelRequirements().setPanelPreferences(TestingUtil.panelPreferences());
        hearingDetails.getPanelRequirements().setPanelSpecialisms(List.of("Specialism 1"));
        hearingDetails.setHearingIsLinkedFlag(false);
        hearingDetails.setPanelRequirements(panelRequirementsList());
        hearingDetails.setHearingChannels(getHearingChannelsList());
        return hearingDetails;
    }

    public static PanelRequirements panelRequirementsList() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(List.of("RoleType1", "RoleType2"));
        panelRequirements.setAuthorisationTypes(List.of("AuthorisationType1", "AuthorisationType2"));
        panelRequirements.setAuthorisationSubType(List.of("AuthorisationSubType2", "AuthorisationSubType2"));
        return panelRequirements;
    }

    public static List<PanelPreference> panelPreferences() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setRequirementType("MUSTINC");
        panelPreference.setMemberType("Member type 1");
        panelPreference.setMemberID("MID123");
        PanelPreference panelPreferenceTwo = new PanelPreference();
        panelPreferenceTwo.setRequirementType("OPTINC");
        panelPreferenceTwo.setMemberType("Member type 2");
        panelPreferenceTwo.setMemberID("MID999");
        List<PanelPreference> panelPreferences = List.of(panelPreference, panelPreferenceTwo);
        return panelPreferences;
    }

    public static IndividualDetails allIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mrs");
        individualDetails.setFirstName("Rosie");
        individualDetails.setLastName("Jason");
        individualDetails.setPreferredHearingChannel("channel 5");
        individualDetails.setInterpreterLanguage("French");
        individualDetails.setReasonableAdjustments(List.of("Adjust1", "Adjust2", "Adjust3"));
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("More vulnerable");
        individualDetails.setHearingChannelPhone(List.of("01111111111"));
        individualDetails.setHearingChannelEmail(List.of("hearing.channel_udated@email.com"));
        List<RelatedParty> relatedParties = new ArrayList<>();
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("P1");
        relatedParty1.setRelationshipType("Mother");
        relatedParties.add(relatedParty1);
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("P2");
        relatedParty2.setRelationshipType("Father");
        relatedParties.add(relatedParty2);
        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    public static LocalDateTime convertDateTime(String dateStr) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateStr, format);
    }

    public static HearingActual hearingActualWithDuplicatedHearingDate() {
        return hearingActual(
            hearingActualsOutcome(),
            List.of(
                actualHearingDay(LocalDate.of(2022, 3, 23)),
                actualHearingDay(LocalDate.of(2022, 3, 23))
            )
        );
    }

    public static HearingActual hearingActualWithHearingDateInFuture() {
        return hearingActual(
            hearingActualsOutcome(),
            List.of(
                actualHearingDay(LocalDate.of(2022, 3, 23)),
                actualHearingDay(LocalDate.of(2922, 3, 23))
            )
        );
    }

    public static HearingActual hearingActualWithHearingDates(List<ActualHearingDay> actualHearingDays) {
        return hearingActual(hearingActualsOutcome(), actualHearingDays);
    }

    public static HearingActual hearingActual() {
        HearingActual request = new HearingActual();
        request.setHearingOutcome(hearingActualsOutcome());
        request.setActualHearingDays(List.of(actualHearingDay(LocalDate.of(2022, 1, 28))));

        return request;
    }

    public static HearingActual hearingActual(HearingActualsOutcome outcome, List<ActualHearingDay> actualHearingDays) {
        HearingActual request = new HearingActual();
        request.setHearingOutcome(outcome);
        request.setActualHearingDays(actualHearingDays);
        return request;
    }

    public static HearingActualsOutcome hearingActualsOutcome() {
        HearingActualsOutcome hearingActualsOutcome = new HearingActualsOutcome();
        hearingActualsOutcome.setHearingType("Witness Statement");
        hearingActualsOutcome.setHearingFinalFlag(false);
        hearingActualsOutcome.setHearingResult("COMPLETED");
        hearingActualsOutcome.setHearingResultReasonType("Nothing more to hear");
        hearingActualsOutcome.setHearingResultDate(LocalDate.of(2022, 2, 1));
        return hearingActualsOutcome;
    }

    public static HearingActualsOutcome hearingActualsOutcome(String hearingResult,
                                                              String hearingResultReasonType) {
        HearingActualsOutcome hearingActualsOutcome = hearingActualsOutcome();
        hearingActualsOutcome.setHearingResult(hearingResult);
        hearingActualsOutcome.setHearingResultReasonType(hearingResultReasonType);
        return hearingActualsOutcome;
    }

    public static ActualHearingDay actualHearingDay(LocalDate hearingDate) {
        ActualHearingDay actualHearingDay = new ActualHearingDay();
        actualHearingDay.setHearingDate(hearingDate);
        actualHearingDay.setHearingStartTime(LocalDateTime.of(hearingDate, LocalTime.of(10, 0)));
        actualHearingDay.setHearingEndTime(LocalDateTime.of(hearingDate, LocalTime.of(15, 0)));
        actualHearingDay.setPauseDateTimes(List.of(actualHearingPauseDateTime(
            LocalDateTime.of(hearingDate, LocalTime.of(12, 0)),
            LocalDateTime.of(hearingDate, LocalTime.of(12, 30))
        )));

        actualHearingDay.setActualDayParties(List.of(
            actualHearingParty(123L, "43333", actualHearingPartyDetails("WitnessForeName1", "WitnessLastName1"),
                               "claiming party", false
            )
        ));
        return actualHearingDay;
    }

    private static ActualHearingDayParties actualHearingParty(Long partyId,
                                                              String partyRole,
                                                              ActualHearingDayPartyDetail partyDetail,
                                                              String partyChannelSubType,
                                                              Boolean didNotAttendFlag) {
        ActualHearingDayParties parties = new ActualHearingDayParties();

        parties.setActualPartyId(partyId.toString());
        parties.setPartyRole(partyRole);
        parties.setIndividualDetails(partyDetail);
        parties.setPartyChannelSubType(partyChannelSubType);
        parties.setDidNotAttendFlag(didNotAttendFlag);

        return parties;
    }

    private static ActualHearingDayPartyDetail actualHearingPartyDetails(String firstName, String lastName) {
        ActualHearingDayPartyDetail partyDetail = new ActualHearingDayPartyDetail();
        partyDetail.setFirstName(firstName);
        partyDetail.setLastName(lastName);
        return partyDetail;
    }

    private static ActualHearingDayPauseDayTime actualHearingPauseDateTime(LocalDateTime pauseStartTime,
                                                                           LocalDateTime pauseEndTime) {
        ActualHearingDayPauseDayTime pauseDayTime = new ActualHearingDayPauseDayTime();
        pauseDayTime.setPauseStartTime(pauseStartTime);
        pauseDayTime.setPauseEndTime(pauseEndTime);
        return pauseDayTime;
    }

    public static UpdateHearingRequest updateHearingRequestWithoutHearingWindow(int version) {
        UpdateHearingRequest request = new UpdateHearingRequest();
        HearingDetails hearingDetails = hearingDetailsWithoutHearingWindow();
        hearingDetails.setAmendReasonCodes(List.of("reason 1", "reason 2"));
        request.setHearingDetails(hearingDetails);
        CaseDetails caseDetails = getValidCaseDetails();
        request.setCaseDetails(caseDetails);
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(1);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static HearingDetails hearingDetailsWithoutHearingWindow() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(false);
        hearingDetails.setHearingType("Some hearing type");
        hearingDetails.setDuration(360);
        hearingDetails.setNonStandardHearingDurationReasons(List.of("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setHearingIsLinkedFlag(Boolean.TRUE);
        hearingDetails.setHearingChannels(getHearingChannelsList());
        HearingLocation location1 = new HearingLocation();
        location1.setLocationType(LocationType.CLUSTER.getLabel());
        location1.setLocationId("Location Id");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setFacilitiesRequired(List.of("facility1", "facility2"));
        return hearingDetails;
    }
}

