package uk.gov.hmcts.reform.hmc.utils;

import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
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
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.LocationId;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.UpdateRequestDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;

public class TestingUtil {

    public static final String CASE_REFERENCE = "1111222233334444";

    private TestingUtil() {
    }

    public static RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    public static HearingDetails hearingDetails() {

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        hearingWindow.setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(0);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("COURT");
        location1.setLocationType("Location type");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        return hearingDetails;
    }

    public static PanelRequirements panelRequirements() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(Arrays.asList("RoleType1"));
        panelRequirements.setAuthorisationTypes(Arrays.asList("AuthorisationType1"));
        panelRequirements.setAuthorisationSubType(Arrays.asList("AuthorisationSubType2"));
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
        requiredLocationsEntity.setLocationId(LocationId.CLUSTER);
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

    public static CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef(CASE_REFERENCE);
        caseDetails.setRequestTimeStamp(LocalDateTime.parse("2021-08-10T12:20:00"));
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

    public static List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("ind");
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");

        List<PartyDetails> partyDetails = Lists.newArrayList(partyDetails1, partyDetails2);
        return partyDetails;
    }

    public static List<CaseCategoriesEntity> caseCategoriesEntities() {
        CaseCategoriesEntity caseCategoriesEntity = new CaseCategoriesEntity();
        caseCategoriesEntity.setCategoryType(CaseCategoryType.CASETYPE);
        caseCategoriesEntity.setCaseCategoryValue("PROBATE");
        return Arrays.asList(caseCategoriesEntity);
    }

    public static IndividualDetails individualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
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
        hearingEntity.setStatus(HEARING_STATUS);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    public static CaseHearingRequestEntity caseHearingRequestEntity() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        entity.setAutoListFlag(false);
        entity.setHearingType("Some hearing type");
        entity.setRequiredDurationInMinutes(10);
        entity.setHearingPriorityType("Priority type");
        entity.setHmctsServiceID("ABA1");
        entity.setCaseReference("1111222233334444");
        entity.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity.setCaseUrlContextPath("https://www.google.com");
        entity.setHmctsInternalCaseName("Internal case name");
        entity.setOwningLocationId("CMLC123");
        entity.setCaseRestrictedFlag(true);
        entity.setCaseSlaStartDate(LocalDate.parse("2020-08-10"));
        entity.setVersionNumber(1);
        entity.setRequestTimeStamp(LocalDateTime.parse("2020-08-10T12:20:00"));
        return entity;

    }

    public static IndividualDetails individualContactDetails_HearingChannelEmail() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("hearing.channel@email.com");
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails_HearingChannelPhone() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelPhone("01234567890");
        return individualDetails;
    }

    public static IndividualDetails individualContactDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("hearing.channel@email.com");
        individualDetails.setHearingChannelPhone("01234567890");
        return individualDetails;

    }

    public static DeleteHearingRequest deleteHearingRequest() {
        DeleteHearingRequest request = new DeleteHearingRequest();
        request.setVersionNumber(1);
        request.setCancellationReasonCode("test");
        return request;
    }

    public static UpdateHearingRequest updateHearingRequest() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        UpdateRequestDetails requestDetails = new UpdateRequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        requestDetails.setVersionNumber(1);
        request.setRequestDetails(requestDetails);
        return request;
    }

    public static HearingEntity deleteHearingEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity());
        hearingEntity.setStatus(CANCELLATION_REQUESTED);
        return hearingEntity;
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
        getHearingsResponse.setHmctsServiceId("AB1A");
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(2000000000L);
        caseHearing.setHearingRequestDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseHearing.setHearingType("45YAO6VflHAmYy7N85fv");
        caseHearing.setHmcStatus("HEARING_REQUESTED");
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.parse("2020-08-10T12:20:00"));
        caseHearing.setListAssistCaseStatus("EXCEPTION");
        caseHearing.setHearingListingStatus("listingStatus");
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
        caseHearing.setHearingDaySchedule(Arrays.asList(schedule));
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);
        getHearingsResponse.setCaseHearings(caseHearingList);
        return getHearingsResponse;
    }

    public static HearingActual hearingActualWithDuplicatedHearingDate() {
        return hearingActual(hearingActualsOutcome(),
                             Arrays.asList(actualHearingDay(LocalDate.of(2022, 3, 23)),
                                           actualHearingDay(LocalDate.of(2022, 3, 23))));
    }

    public static HearingActual hearingActualWithHearingDateInFuture() {
        return hearingActual(hearingActualsOutcome(),
                             Arrays.asList(actualHearingDay(LocalDate.of(2022, 3, 23)),
                                           actualHearingDay(LocalDate.of(2922, 3, 23))));
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
                               "claiming party", false)
        ));
        return actualHearingDay;
    }

    private static ActualHearingDayParties actualHearingParty(Long partyId,
                                                              String partyRole,
                                                              ActualHearingDayPartyDetail partyDetail,
                                                              String partyChannelSubType,
                                                              Boolean didNotAttendFlag) {
        ActualHearingDayParties parties = new ActualHearingDayParties();

        parties.setActualPartyId(partyId);
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

    public static CaseHearingRequestEntity getCaseHearingsEntities() {
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        entity.setCaseHearingID(2000000000L);
        hearingEntity.setStatus("HEARING_REQUESTED");
        entity.setHearing(hearingEntity);
        entity.setHmctsServiceID("ABA1");
        entity.setCaseReference("12345");
        entity.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity.setHearingType("Some hearing type");
        entity.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntity()));
        entity.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity()));
        return entity;
    }


    public static List<NonStandardDurationsEntity> getNonStandardDurationEntities() {
        NonStandardDurationsEntity nonStandardDurationsEntity = new NonStandardDurationsEntity();
        nonStandardDurationsEntity.setNonStandardHearingDurationReasonType("Reason");
        return Arrays.asList(nonStandardDurationsEntity);
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
        entity1.setHmctsServiceID("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntity()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity()));
        entities.add(entity1);
    }

    private static void getSecondEntity(List<CaseHearingRequestEntity> entities) {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000001L);
        entity1.setCaseHearingID(2000000001L);
        hearingEntity.setStatus("HEARING_UPDATED");
        entity1.setHearing(hearingEntity);
        entity1.setHmctsServiceID("ABA1");
        entity1.setCaseReference("4567");
        entity1.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntity()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity()));
        entities.add(entity1);
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntity(LocalDateTime startDateTime) {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(startDateTime);
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setVenueId("venue1");
        entity.setRoomId("room1");
        entity.setListAssistSessionId("session1");
        entity.setHearingAttendeeDetails(Arrays.asList(hearingAttendeeDetailsEntity()));
        entity.setHearingDayPanel(Arrays.asList(hearingDayPanelEntities()));
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntity() {
        return hearingDayDetailsEntity(LocalDateTime.parse("2020-08-10T12:20:00"));
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
            hearingEntity.setCaseHearingRequest(caseHearingRequestEntityWithPartyOrg());
        } else {
            hearingEntity.setCaseHearingRequest(caseHearingRequestEntityWithPartyInd());
        }
        hearingEntity.getCaseHearingRequest().setVersionNumber(1);
        return hearingEntity;
    }

    public static HearingEntity getCaseHearingsEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(2000000000L);
        hearingEntity.setStatus("HEARING_REQUESTED");
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity()));
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntityWithPartyOrg());

        hearingEntity.getCaseHearingRequest().setVersionNumber(1);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(hearingPartyEntityOrg()));
        return hearingEntity;
    }

    public static HearingResponseEntity hearingResponseEntity() {
        return hearingResponseEntity("1", "1", LocalDateTime.parse("2020-08-10T12:20:00"),
                                     List.of(hearingDayDetailsEntity()));
    }

    public static HearingResponseEntity hearingResponseEntity(String version, String requestVersion,
                                                              LocalDateTime requestTimestamp,
                                                              List<HearingDayDetailsEntity> hearingDayDetailsEntities) {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setResponseVersion(version);
        entity.setRequestVersion(requestVersion);
        entity.setRequestTimeStamp(requestTimestamp);
        entity.setHearingResponseId(2L);
        entity.setListingStatus("listingStatus");
        entity.setListingCaseStatus("Case_listingStatus");
        entity.setHearingDayDetails(hearingDayDetailsEntities);
        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityOrg() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.ORG);
        entity.setPartyRoleType("role");
        entity.setUnavailabilityEntity(Arrays.asList(unavailabilityEntity()));
        entity.setOrganisationDetailEntity(organisationDetailEntity());

        return entity;
    }

    public static HearingPartyEntity hearingPartyEntityInd() {
        HearingPartyEntity entity = new HearingPartyEntity();
        entity.setPartyReference("reference");
        entity.setPartyType(PartyType.IND);
        entity.setPartyRoleType("role");
        entity.setIndividualDetailEntity(Arrays.asList(individualDetailEntity()));

        return entity;
    }

    private static CaseHearingRequestEntity caseHearingRequestEntityWithPartyOrg() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setCaseHearingID(2000000000L);
        entity1.setHmctsServiceID("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity1.setHearingType("Some hearing type");
        return entity1;
    }

    private static CaseHearingRequestEntity caseHearingRequestEntityWithPartyInd() {
        CaseHearingRequestEntity entity1 = new CaseHearingRequestEntity();
        entity1.setCaseHearingID(2000000000L);

        entity1.setHearing(getCaseHearingsEntity());
        entity1.setHmctsServiceID("ABA1");
        entity1.setCaseReference("12345");
        entity1.setHearingRequestReceivedDateTime(LocalDateTime.parse("2000-08-10T12:20:00"));
        entity1.setHearingType("Some hearing type");
        entity1.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntity()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity()));
        entity1.setHearingParties(Arrays.asList(hearingPartyEntityInd()));

        return entity1;
    }
}

