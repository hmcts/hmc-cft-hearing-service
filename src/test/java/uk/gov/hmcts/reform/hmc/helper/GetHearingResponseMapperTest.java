package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class GetHearingResponseMapperTest {

    @Test
    void toHearingsResponseWhenDataIsPresentForOrg() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));


        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertOrgDetails(response.getPartyDetails().get(0).getOrganisationDetails());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentForInd() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.IND);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityInd()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));


        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "IND");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertIndividualDetails(response.getPartyDetails().get(0).getIndividualDetails());

    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithCaseCategories() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequest().setCaseCategories(TestingUtil.caseCategoriesEntities());

        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertCaseCategory(response.getCaseDetails().getCaseCategories().get(0));
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithHearingPriority() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequest().setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());

        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertNonStandardDuration(response.getHearingDetails().getNonStandardHearingDurationReasons());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithPanelRequirements() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequest()
            .setPanelRequirements(Arrays.asList(TestingUtil.panelRequirementsEntity()));

        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertPanelRequirements(response.getHearingDetails().getPanelRequirements());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithFacilityType() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequest().setRequiredFacilities(Arrays.asList(TestingUtil.facilityEntity()));

        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertFacility(response.getHearingDetails().getFacilitiesRequired());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithHearingLocations() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequest().setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequest().setRequiredLocations(Arrays.asList(TestingUtil.locationEntity()));

        GetHearingResponseMapper getHearingResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), "ORG");
        assertHearingResponse(response.getHearingResponse().get(0));
        assertHearingDaySchedule(response.getHearingResponse().get(0).getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().get(0).getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertHearingLocation(response.getHearingDetails().getHearingLocations().get(0));
    }


    private void assertRequestDetails(RequestDetails requestDetails) {
        assertAll(
            () -> assertEquals(requestDetails.getStatus(), "HEARING_REQUESTED"),
            () -> assertEquals(requestDetails.getVersionNumber(), 1)
        );
    }

    private void assertCaseDetails(CaseDetails caseDetails) {
        assertAll(
            () -> assertEquals(caseDetails.getHmctsServiceCode(), "ABA1"),
            () -> assertEquals(caseDetails.getCaseRef(), "12345")
        );
    }

    private void assertHearingDetails(HearingDetails hearingDetails) {
        assertAll(
            () -> assertEquals(hearingDetails.getHearingType(), "Some hearing type")
        );
    }

    private void assertPartyDetails(PartyDetails partyDetails, String partyType) {
        assertAll(
            () -> assertEquals(partyDetails.getPartyID(), "reference"),
            () -> assertEquals(partyDetails.getPartyType(), partyType),
            () -> assertEquals(partyDetails.getPartyRole(), "role")
        );
    }

    private void assertHearingResponse(HearingResponse hearingResponse) {
        assertAll(
            () -> assertEquals(hearingResponse.getLaCaseStatus(), "Case_listingStatus"),
            () -> assertEquals(hearingResponse.getListingStatus(), "listingStatus"),
            () -> assertEquals(hearingResponse.getResponseVersion(), 2)
        );
    }

    private void assertHearingDaySchedule(HearingDaySchedule hearingDaySchedule) {
        assertAll(
            () -> assertEquals(hearingDaySchedule.getListAssistSessionId(), "session1"),
            () -> assertEquals(hearingDaySchedule.getHearingVenueId(), "venue1"),
            () -> assertEquals(hearingDaySchedule.getHearingRoomId(), "room1"),
            () -> assertEquals(hearingDaySchedule.getPanelMemberId(), "PanelUser1")
        );
    }

    private void assertAttendees(Attendee attendee) {
        assertAll(
            () -> assertEquals(attendee.getPartyId(), "Party1"),
            () -> assertEquals(attendee.getHearingSubChannel(), "SubChannel1")
        );
    }

    private void assertNonStandardDuration(List<String> hearingPriorityType) {
        assertAll(
            () -> assertEquals(hearingPriorityType.get(0), "Reason")
        );
    }

    private void assertCaseCategory(CaseCategory caseCategory) {
        assertAll(
            () -> assertEquals(caseCategory.getCategoryType(), "caseType"),
            () -> assertEquals(caseCategory.getCategoryValue(), "PROBATE")
        );
    }

    private void assertUnavailability(UnavailabilityRanges unavailabilityRanges, UnavailabilityDow unavailabilityDow) {
        assertAll(
            () -> assertEquals(
                unavailabilityRanges.getUnavailableToDate(),
                LocalDate.of(2020, 12, 20)
            ),
            () -> assertEquals(
                unavailabilityRanges.getUnavailableFromDate(),
                LocalDate.of(2020, 12, 20)
            ),
            () -> assertEquals(unavailabilityDow.getDowUnavailabilityType(), "ALL"),
            () -> assertEquals(unavailabilityDow.getDow(), "Friday")
        );
    }

    private void assertOrgDetails(OrganisationDetails organisationDetails) {
        assertAll(
            () -> assertEquals(organisationDetails.getOrganisationType(), "code"),
            () -> assertEquals(organisationDetails.getCftOrganisationID(), "reference"),
            () -> assertEquals(organisationDetails.getName(), "name")
        );
    }

    private void assertIndividualDetails(IndividualDetails individualDetails) {
        assertAll(
            () -> assertEquals(individualDetails.getTitle(), "mr"),
            () -> assertEquals(individualDetails.getFirstName(), "joe"),
            () -> assertEquals(individualDetails.getLastName(), "bloggs"),
            () -> assertEquals(individualDetails.getPreferredHearingChannel(), "channelType"),
            () -> assertEquals(individualDetails.getInterpreterLanguage(), "english"),
            () -> assertEquals(individualDetails.getVulnerableFlag(), true),
            () -> assertEquals(individualDetails.getVulnerabilityDetails(), "details")
        );
    }

    private void assertPanelRequirements(PanelRequirements panelRequirements) {
        assertAll(
            () -> assertEquals(panelRequirements.getRoleType(), Arrays.asList("RoleType1"))
        );
    }

    private void assertFacility(List<String> facilityType) {
        assertAll(
            () -> assertEquals(facilityType.get(0), "RoleType1")
        );
    }

    private void assertHearingLocation(HearingLocation hearingLocation) {
        assertAll(
            () -> assertNull(hearingLocation.getLocationType()),
            () -> assertEquals(hearingLocation.getLocationId(), "cluster")
        );
    }

}
