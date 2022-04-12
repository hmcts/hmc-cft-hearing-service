package uk.gov.hmcts.reform.hmc.utils;

import org.assertj.core.util.Lists;
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
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
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
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;

public class TestingUtil {

    public static final String CASE_REFERENCE = "1111222233334444";

    private TestingUtil() {
    }

    public static RequestDetails requestDetails() {
        return new RequestDetails();
    }

    public static HearingDetails hearingDetails() {

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingWindow.setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(0);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setHearingIsLinkedFlag(Boolean.TRUE);
        HearingLocation location1 = new HearingLocation();
        location1.setLocationType(LocationType.CLUSTER.getLabel());
        location1.setLocationId("Location Id");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setFacilitiesRequired(Arrays.asList("facility1", "facility2"));
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

    public static UnavailabilityEntity unavailabilityEntity() {
        UnavailabilityEntity unavailabilityEntity = new UnavailabilityEntity();
        unavailabilityEntity.setDayOfWeekUnavailable(DayOfWeekUnavailable.FRIDAY);
        unavailabilityEntity.setEndDate(LocalDate.of(2020, 12, 20));
        unavailabilityEntity.setDayOfWeekUnavailableType(DayOfWeekUnAvailableType.ALL);
        unavailabilityEntity.setStartDate(LocalDate.of(2020, 12, 20));
        unavailabilityEntity.setUnAvailabilityType(UNAVAILABILITY_DOW_TYPE);
        return unavailabilityEntity;
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
        individualDetailEntity.setRelatedPartyRelationshipType("relationshipType");
        individualDetailEntity.setRelatedPartyID("id");
        individualDetailEntity.setVulnerabilityDetails("details");
        return individualDetailEntity;
    }

    public static List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("ind");
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");

        return Lists.newArrayList(partyDetails1, partyDetails2);
    }

    public static List<CaseCategoriesEntity> caseCategoriesEntities() {
        CaseCategoriesEntity caseCategoriesEntity = new CaseCategoriesEntity();
        caseCategoriesEntity.setCategoryType(CaseCategoryType.CASETYPE);
        caseCategoriesEntity.setCaseCategoryValue("PROBATE");
        return List.of(caseCategoriesEntity);
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

    public static HearingEntity hearingEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setStatus(POST_HEARING_STATUS);
        hearingEntity.setIsLinkedFlag(Boolean.TRUE);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));
        return hearingEntity;
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
        request.setCancellationReasonCode("test");
        return request;
    }

    public static UpdateHearingRequest updateHearingRequest(int version) {
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(version);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static UpdateHearingRequest updateHearingRequest() {
        return updateHearingRequest(1);
    }

    public static UpdateHearingRequest validUpdateHearingRequest() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails());
        CaseDetails caseDetails = getValidCaseDetails();
        request.setCaseDetails(caseDetails);
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(1);
        request.setRequestDetails(requestDetails);
        return request;
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

    public static GetHearingsResponse getHearingsResponseWhenDataIsPresent(String caseRef) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setHmctsServiceCode("AB1A");
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(2000000000L);
        caseHearing.setHearingRequestDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseHearing.setHearingType("45YAO6VflHAmYy7N85fv");
        caseHearing.setHmcStatus("HEARING_REQUESTED");
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
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);
        getHearingsResponse.setCaseHearings(caseHearingList);
        return getHearingsResponse;
    }

    public static CaseHearingRequestEntity getCaseHearingsEntities() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setIsLinkedFlag(Boolean.TRUE);
        entity.setCaseHearingID(2000000000L);
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
        hearingEntity.setId(2000000000L);
        entity1.setCaseHearingID(2000000000L);
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
        entity.setListAssistSessionId("session1");
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
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
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
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntityWithPartyOrg()));

        hearingEntity.getCaseHearingRequests().get(0).setVersionNumber(1);
        hearingEntity.getCaseHearingRequests().get(0).setHearingParties(Arrays.asList(hearingPartyEntityOrg()));
        return hearingEntity;
    }

    public static HearingEntity getHearingsEntityForHearingActuals(String status) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus(status);
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntityWithPartyOrg());

        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
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
        caseHearingRequestEntity.setHearingParties(Arrays.asList(hearingPartyEntityOrg()));
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities());

        hearingEntity.getHearingResponses().get(0).setActualHearingEntity(actualHearingEntity(PartyType.ORG));
        return hearingEntity;
    }

    public static HearingEntity getHearingsEntityForHearingActualsIndividual() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntityWithPartyInd());

        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
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
        caseHearingRequestEntity.setHearingParties(Arrays.asList(hearingPartyEntityInd()));
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
        entity.setActualHearingDay(Arrays.asList(actualHearingDayEntity(partyType)));
        entity.setActualHearingIsFinalFlag(true);
        return entity;
    }

    public static ActualPartyRelationshipDetailEntity actualPartyRelationshipDetailEntity(
        ActualHearingPartyEntity actualHearingPartyEntity) {
        ActualPartyRelationshipDetailEntity entity = new ActualPartyRelationshipDetailEntity();
        entity.setActualPartyRelationshipId(1L);
        entity.setTargetActualPartyId(1L);
        entity.setActualHearingParty(actualHearingPartyEntity);
        return entity;
    }

    public static ActualHearingDayEntity actualHearingDayEntity(PartyType partyType) {
        ActualHearingDayEntity entity = new ActualHearingDayEntity();
        entity.setHearingDate(LocalDate.of(2000, 01, 01));
        entity.setStartDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setActualHearingDayPauses(Arrays.asList(actualHearingDayPausesEntity()));
        entity.setActualHearingParty(Arrays.asList(actualHearingPartyEntity(partyType)));
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
        entity.setActualAttendeeIndividualDetail(Arrays.asList(actualAttendeeIndividualDetailEntity(partyType)));
        entity.setActualPartyRelationshipDetail(Arrays.asList(actualPartyRelationshipDetailEntity(entity)));
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
        entity.setListingStatus(ListingStatus.FIXED.name());
        entity.setListingCaseStatus(ListAssistCaseStatus.CASE_CREATED.name());
        entity.setCancellationReasonType("Cancelled Reason 1");
        entity.setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity()));
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntity() {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(LocalDateTime.of(2000, 8, 10, 12, 20));
        entity.setEndDateTime(LocalDateTime.of(2000, 8, 10, 12, 20));
        entity.setRoomId("roomId");
        entity.setListAssistSessionId("sessionId");
        entity.setVenueId("venueId");

        HearingAttendeeDetailsEntity attendee = new HearingAttendeeDetailsEntity();
        entity.setHearingAttendeeDetails(List.of(attendee));
        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityOrg() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.ORG);
        entity.setPartyRoleType("role");
        entity.setUnavailabilityEntity(List.of(unavailabilityEntity()));
        entity.setOrganisationDetailEntity(organisationDetailEntity());

        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityInd() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.IND);
        entity.setPartyRoleType("role");
        entity.setContactDetails(List.of(contactDetailsEntity_Email(), contactDetailsEntity_Phone()));
        entity.setIndividualDetailEntity(List.of(individualDetailEntity()));
        return entity;
    }

    private static CaseHearingRequestEntity caseHearingRequestEntityWithPartyOrg() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setVersionNumber(1);
        entity1.setCaseHearingID(2000000000L);
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        return entity1;
    }

    private static CaseHearingRequestEntity caseHearingRequestEntityWithPartyInd() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setVersionNumber(1);
        entity1.setCaseHearingID(2000000000L);

        entity1.setHearing(getCaseHearingsEntity());
        entity1.setHmctsServiceCode("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(List.of(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(List.of(hearingDayDetailsEntities()));
        entity1.setHearingParties(List.of(hearingPartyEntityInd()));
        return entity1;
    }

    public static UpdateHearingRequest updateHearingRequestWithPartyDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(1);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setRequestDetails(requestDetails);
        request.setHearingDetails(hearingDetailsWithAllFields());
        request.setCaseDetails(getValidCaseDetails());
        request.setPartyDetails(partyDetailsWith2Parties());

        return request;
    }

    private static List<PartyDetails> partyDetailsWith2Parties() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("ind");
        partyDetails1.setPartyRole("DEF");
        partyDetails1.setIndividualDetails(allIndividualDetails());
        partyDetails1.setUnavailabilityDow(unavAilabilityDowDetails());
        partyDetails1.setUnavailabilityRanges(unAvailabilityRanges());

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");
        partyDetails2.setOrganisationDetails(organisationDetails());

        List<PartyDetails> partyDetails = Lists.newArrayList(partyDetails1, partyDetails2);
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
        List<UnavailabilityRanges> unavailabilityRanges = Lists.newArrayList(detail1, detail2);
        return unavailabilityRanges;
    }

    private static List<UnavailabilityDow> unavAilabilityDowDetails() {
        UnavailabilityDow detail1 = new UnavailabilityDow();
        detail1.setDowUnavailabilityType("PM");
        detail1.setDow("MONDAY");
        UnavailabilityDow detail2 = new UnavailabilityDow();
        detail2.setDowUnavailabilityType("All Day");
        detail2.setDow("THURSDAY");
        List<UnavailabilityDow> unavailabilityDows = Lists.newArrayList(detail1, detail2);
        return unavailabilityDows;
    }

    public static HearingDetails hearingDetailsWithAllFields() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingWindow.setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(60);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        hearingDetails.setNumberOfPhysicalAttendees(4);
        hearingDetails.setHearingInWelshFlag(false);
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("COURT");
        location1.setLocationType("Court");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setFacilitiesRequired(Arrays.asList("facility1", "facility2"));
        hearingDetails.setListingComments("Some listing comments");
        hearingDetails.setHearingRequester("Some judge");
        hearingDetails.setPrivateHearingRequiredFlag(false);
        hearingDetails.setLeadJudgeContractType("AB123");
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        hearingDetails.getPanelRequirements().setPanelPreferences(TestingUtil.panelPreferences());
        hearingDetails.getPanelRequirements().setPanelSpecialisms(Arrays.asList("Specialism 1"));
        hearingDetails.setHearingIsLinkedFlag(false);
        hearingDetails.setPanelRequirements(panelRequirementsList());
        return hearingDetails;
    }

    public static PanelRequirements panelRequirementsList() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(Arrays.asList("RoleType1","RoleType2"));
        panelRequirements.setAuthorisationTypes(Arrays.asList("AuthorisationType1","AuthorisationType2"));
        panelRequirements.setAuthorisationSubType(Arrays.asList("AuthorisationSubType2","AuthorisationSubType2"));
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
        List<PanelPreference> panelPreferences = Arrays.asList(panelPreference, panelPreferenceTwo);
        return panelPreferences;
    }

    public static IndividualDetails allIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mrs");
        individualDetails.setFirstName("Rosie");
        individualDetails.setLastName("Jason");
        individualDetails.setPreferredHearingChannel("channel 5");
        individualDetails.setInterpreterLanguage("French");
        individualDetails.setReasonableAdjustments(Arrays.asList("Adjust1","Adjust2","Adjust3"));
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("More vulnerable");
        individualDetails.setHearingChannelPhone(List.of("01111111111"));
        individualDetails.setHearingChannelEmail(List.of("hearing.channel_udated@email.com"));
        List<RelatedParty> relatedParties = new ArrayList<>();
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("Party1");
        relatedParty1.setRelationshipType("Mother");
        relatedParties.add(relatedParty1);
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("Party2");
        relatedParty2.setRelationshipType("Father");
        relatedParties.add(relatedParty2);
        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    public static LocalDateTime convertDateTime(String dateStr) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateStr, format);
    }
}

