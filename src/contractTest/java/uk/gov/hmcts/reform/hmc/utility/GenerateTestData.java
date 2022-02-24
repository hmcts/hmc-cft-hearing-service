package uk.gov.hmcts.reform.hmc.utility;

import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
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

public class GenerateTestData {

    public static final String CASE_REFERENCE = "1111222233334444";

    protected GenerateTestData(){
        // not called
    }

    protected static DeleteHearingRequest generateDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCode("REASONCODE25");
        deleteHearingRequest.setVersionNumber(2);
        return  deleteHearingRequest;
    }

    protected static CreateHearingRequest generateCreateHearingRequest() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(generateRequestDetails());
        createHearingRequest.setHearingDetails(generateHearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(generatePanelRequirements());
        createHearingRequest.setCaseDetails(generateCaseDetails());
        createHearingRequest.setPartyDetails(generatePartyDetails());
        return createHearingRequest;
    }

    /**
     * generate Update Hearing Request.
     *
     * @return HearingRequest hearing request
     */
    protected static UpdateHearingRequest generateUpdateHearingRequest() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setRequestDetails(generateUpdateRequestDetails());
        request.setHearingDetails(generateHearingDetails());
        request.setCaseDetails(generateCaseDetails());
        request.setPartyDetails(generatePartyDetails());
        return request;
    }

    protected static RequestDetails generateRequestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    protected static UpdateRequestDetails generateUpdateRequestDetails() {
        UpdateRequestDetails requestDetails = new UpdateRequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        requestDetails.setVersionNumber(45);
        return requestDetails;
    }

    protected static HearingDetails generateHearingDetails() {
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

    protected static PanelRequirements generatePanelRequirements() {
        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(Arrays.asList("RoleType1"));
        panelRequirements.setAuthorisationTypes(Arrays.asList("AuthorisationType1"));
        panelRequirements.setAuthorisationSubType(Arrays.asList("AuthorisationSubType2"));
        return panelRequirements;
    }

    protected static CaseDetails generateCaseDetails() {
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

    protected static List<PartyDetails> generatePartyDetails() {
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

    protected static IndividualDetails generateIndividualDetails() {
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

    protected static IndividualDetails generateIndividualDetailsWithRelatedPartyDetails() {
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

    protected static IndividualDetails generateIndividualWithoutRelatedPartyDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        return individualDetails;
    }

    protected static OrganisationDetails generateOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("type");
        organisationDetails.setCftOrganisationID("cft");
        return organisationDetails;
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
        request.setHearingDetails(generateHearingDetails());
        request.setCaseDetails(generateCaseDetails());
        request.getHearingDetails().setPanelRequirements(generatePanelRequirements());
        UpdateRequestDetails requestDetails = new UpdateRequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        requestDetails.setVersionNumber(1);
        request.setRequestDetails(requestDetails);
        return request;
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

    public static GetHearingsResponse getHearingsResponseWhenNoData(String caseRef) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setCaseHearings(new ArrayList<>());
        return getHearingsResponse;
    }

}
