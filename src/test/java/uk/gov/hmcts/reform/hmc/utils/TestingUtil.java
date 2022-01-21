package uk.gov.hmcts.reform.hmc.utils;

import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.UpdateRequestDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;

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
        schedule.setHearingJudgeId(Arrays.asList("judge1"));
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
        entity.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntities()));
        entity.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntities()));
        return entity;
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
        entity1.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntities()));
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
        entity1.getHearing().setHearingResponses(Arrays.asList(hearingResponseEntities()));
        entity1.getHearing().getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(hearingDayDetailsEntities()));
        entities.add(entity1);
    }

    public static HearingResponseEntity hearingResponseEntities() {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setRequestTimeStamp(LocalDateTime.parse("2020-08-10T12:20:00"));
        entity.setHearingResponseId(2L);
        entity.setListingStatus("listingStatus");
        entity.setListingCaseStatus("Case_listingStatus");
        return entity;
    }

    public static HearingDayDetailsEntity hearingDayDetailsEntities() {
        HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
        entity.setStartDateTime(LocalDateTime.parse("2020-08-10T12:20:00"));
        entity.setEndDateTime(LocalDateTime.parse("2021-08-10T12:20:00"));
        entity.setVenueId("venue1");
        entity.setRoomId("room1");
        entity.setListAssistSessionId("session1");
        entity.setHearingAttendeeDetails(Arrays.asList(hearingAttendeeDetailsEntity()));
        entity.setHearingDayPanel(Arrays.asList(hearingDayPanelEntities()));
        return entity;
    }

    public static HearingEntity deleteHearingEntity() {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(1L);
        hearingEntity.setStatus(CANCELLATION_REQUESTED);
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestEntity();
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
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

    public static HearingDayPanelEntity hearingDayPanelEntities() {
        HearingDayPanelEntity entity = new HearingDayPanelEntity();
        entity.setPanelUserId("PanelUser1");
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
}

