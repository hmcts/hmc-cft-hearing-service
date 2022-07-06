package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
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
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PARTY_DETAIL_INDIVIDUAL_PARTY_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PARTY_DETAIL_ORGANISATION_PARTY_TYPE;

@ExtendWith(MockitoExtension.class)
class GetHearingResponseMapperTest {

    @InjectMocks
    private GetHearingResponseMapper getHearingResponseMapper;

    @Test
    void toHearingsResponseWhenDataIsPresentForOrg() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertOrgDetails(response.getPartyDetails().get(0).getOrganisationDetails());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentForInd() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.IND);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityInd()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_INDIVIDUAL_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertIndividualDetails(response.getPartyDetails().get(0).getIndividualDetails());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithCaseCategories() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0).setCaseCategories(TestingUtil.caseCategoriesEntities());

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertCaseCategory(response.getCaseDetails().getCaseCategories().get(0));
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithHearingPriority() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertNonStandardDuration(response.getHearingDetails().getNonStandardHearingDurationReasons());
    }

    @Test
    void toHearingsResponseWhenStatusIsHearingRequested() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity("HEARING_REQUESTED");
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());
        GetHearingResponseMapper getHearingsResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingsResponseMapper.toHearingResponse(hearingEntity);

        assertEquals("HEARING_REQUESTED", response.getRequestDetails().getStatus());
    }

    @Test
    void toHearingsResponseWhenStatusIsListed() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity("LISTED");
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());
        GetHearingResponseMapper getHearingsResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingsResponseMapper.toHearingResponse(hearingEntity);

        assertEquals("AWAITING_ACTUALS", response.getRequestDetails().getStatus());
    }


    @Test
    void toHearingsResponseWhenDataIsPresentWithPanelRequirements() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelRequirements(Arrays.asList(TestingUtil.panelRequirementsEntity()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelSpecialisms(Arrays.asList(TestingUtil.panelSpecialismsEntity()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelAuthorisationRequirements(Arrays.asList(TestingUtil.panelAuthorisationRequirementsEntity()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelUserRequirements(Arrays.asList(TestingUtil.panelUserRequirementsEntity()));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertPanelRequirements(response.getHearingDetails().getPanelRequirements());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithPanelRequirementsUserTypeIsNull() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelRequirements(Arrays.asList(TestingUtil.panelRequirementsEntity()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelSpecialisms(Arrays.asList(TestingUtil.panelSpecialismsEntity()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelAuthorisationRequirements(Arrays.asList(TestingUtil.panelAuthorisationRequirementsEntity()));
        PanelUserRequirementsEntity panelUserRequirementsEntity = TestingUtil.panelUserRequirementsEntity();
        panelUserRequirementsEntity.setUserType(null);
        hearingEntity.getCaseHearingRequests().get(0)
            .setPanelUserRequirements(Arrays.asList(panelUserRequirementsEntity));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertAll(
            () -> assertEquals("MUSTINC", response.getHearingDetails()
                .getPanelRequirements().getPanelPreferences().get(0).getRequirementType()),
            () -> assertNull(response.getHearingDetails().getPanelRequirements()
                                 .getPanelPreferences().get(0).getMemberType()),
            () -> assertEquals("judge1", response.getHearingDetails().getPanelRequirements()
                .getPanelPreferences().get(0).getMemberID())
        );
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithFacilityType() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setRequiredFacilities(Arrays.asList(TestingUtil.facilityEntity()));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertFacility(response.getHearingDetails().getFacilitiesRequired());
    }


    @Test
    void toHearingsResponseWhenDataIsPresentWithHearingLocations() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0).setRequiredLocations(Arrays.asList(TestingUtil.locationEntity()));

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertCaseDetails(response.getCaseDetails());
        assertRequestDetails(response.getRequestDetails());
        assertHearingDetails(response.getHearingDetails());
        assertPartyDetails(response.getPartyDetails().get(0), PARTY_DETAIL_ORGANISATION_PARTY_TYPE);
        assertHearingResponse(response.getHearingResponse());
        assertHearingDaySchedule(response.getHearingResponse().getHearingDaySchedule().get(0));
        assertAttendees(response.getHearingResponse().getHearingDaySchedule().get(0).getAttendees().get(0));
        assertUnavailability(
            response.getPartyDetails().get(0).getUnavailabilityRanges().get(0),
            response.getPartyDetails().get(0).getUnavailabilityDow().get(0)
        );
        assertHearingLocation(response.getHearingDetails().getHearingLocations().get(0));
    }

    @Test
    void toHearingsResponseWhenListingStatusIsMissing() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        hearingEntity.getHearingResponses().get(0)
            .setListingStatus(null);
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertNull(response.getHearingResponse().getListingStatus());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentWithHearingChannel() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.IND);
        hearingEntity.getCaseHearingRequests().get(0).setHearingChannels(TestingUtil.hearingChannelsEntity());
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertHearingChannels(response.getHearingDetails().getHearingChannels());
    }

    @Test
    void toHearingsResponseWhenRequestIdIsNull() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity("LISTED");
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(TestingUtil.hearingPartyEntityOrg()));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());
        GetHearingResponseMapper getHearingsResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingsResponseMapper.toHearingResponse(hearingEntity);

        assertEquals("AWAITING_ACTUALS", response.getRequestDetails().getStatus());
        assertNull(response.getRequestDetails().getHearingGroupRequestId());
    }

    @Test
    void toHearingsResponseWhenUnavailabilityIsEmpty() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity("LISTED");
        HearingPartyEntity partyEntity = TestingUtil.hearingPartyEntityInd();
        partyEntity.setUnavailabilityEntity(new ArrayList<>());
        hearingEntity.getCaseHearingRequests().get(0)
            .setHearingParties(Arrays.asList(partyEntity));
        hearingEntity.getHearingResponses().get(0)
            .setHearingDayDetails(Arrays.asList(TestingUtil.hearingDayDetailsEntities()));
        hearingEntity.getCaseHearingRequests().get(0)
            .setNonStandardDurations(TestingUtil.getNonStandardDurationEntities());
        GetHearingResponseMapper getHearingsResponseMapper = new GetHearingResponseMapper();
        GetHearingResponse response = getHearingsResponseMapper.toHearingResponse(hearingEntity);

        assertEquals("AWAITING_ACTUALS", response.getRequestDetails().getStatus());
        assertNull(response.getRequestDetails().getHearingGroupRequestId());
        assertTrue(response.getPartyDetails().get(0).getUnavailabilityDow().isEmpty());
        assertTrue(response.getPartyDetails().get(0).getUnavailabilityRanges().isEmpty());
    }


    private void assertRequestDetails(RequestDetails requestDetails) {
        assertAll(
            () -> assertEquals("HEARING_REQUESTED", requestDetails.getStatus()),
            () -> assertEquals(1, requestDetails.getVersionNumber())
        );
    }

    private void assertCaseDetails(CaseDetails caseDetails) {
        assertAll(
            () -> assertEquals("ABA1", caseDetails.getHmctsServiceCode()),
            () -> assertEquals("12345", caseDetails.getCaseRef())
        );
    }

    private void assertHearingDetails(HearingDetails hearingDetails) {
        assertAll(
            () -> assertEquals("Some hearing type", hearingDetails.getHearingType())
        );
    }

    private void assertPartyDetails(PartyDetails partyDetails, String partyType) {
        assertAll(
            () -> assertEquals("reference", partyDetails.getPartyID()),
            () -> assertEquals(partyType, partyDetails.getPartyType()),
            () -> assertEquals("role", partyDetails.getPartyRole())
        );
    }

    private void assertHearingResponse(HearingResponse hearingResponse) {
        assertAll(
            () -> assertEquals(ListAssistCaseStatus.CASE_CREATED.name(), hearingResponse.getLaCaseStatus()),
            () -> assertEquals("Fixed", hearingResponse.getListingStatus()),
            () -> assertEquals("Cancelled Reason 1", hearingResponse.getHearingCancellationReason())
        );
    }

    private void assertHearingDaySchedule(HearingDaySchedule hearingDaySchedule) {
        assertAll(
            () -> assertEquals("venue1", hearingDaySchedule.getHearingVenueId()),
            () -> assertEquals("room1", hearingDaySchedule.getHearingRoomId()),
            () -> assertEquals("PanelUser1", hearingDaySchedule.getPanelMemberId())
        );
    }

    private void assertAttendees(Attendee attendee) {
        assertAll(
            () -> assertEquals("Party1", attendee.getPartyId()),
            () -> assertEquals("SubChannel1", attendee.getHearingSubChannel())
        );
    }

    private void assertNonStandardDuration(List<String> hearingPriorityType) {
        assertAll(
            () -> assertEquals("Reason", hearingPriorityType.get(0))
        );
    }

    private void assertCaseCategory(CaseCategory caseCategory) {
        assertAll(
            () -> assertEquals("caseType", caseCategory.getCategoryType()),
            () -> assertEquals("PROBATE", caseCategory.getCategoryValue())
        );
    }

    private void assertUnavailability(UnavailabilityRanges unavailabilityRanges, UnavailabilityDow unavailabilityDow) {
        assertAll(
            () -> assertEquals(
                LocalDate.of(2020, 12, 20),
                unavailabilityRanges.getUnavailableToDate()
            ),
            () -> assertEquals(
                LocalDate.of(2020, 12, 20),
                unavailabilityRanges.getUnavailableFromDate()
            ),
            () -> assertEquals("All Day",
                unavailabilityRanges.getUnavailabilityType()
            ),
            () -> assertEquals("All Day", unavailabilityDow.getDowUnavailabilityType()),
            () -> assertEquals("Friday", unavailabilityDow.getDow())
        );
    }

    private void assertOrgDetails(OrganisationDetails organisationDetails) {
        assertAll(
            () -> assertEquals("code", organisationDetails.getOrganisationType()),
            () -> assertEquals("reference", organisationDetails.getCftOrganisationID()),
            () -> assertEquals("name", organisationDetails.getName())
        );
    }

    private void assertIndividualDetails(IndividualDetails individualDetails) {
        assertAll(
            () -> assertEquals("mr", individualDetails.getTitle()),
            () -> assertEquals("joe", individualDetails.getFirstName()),
            () -> assertEquals("bloggs", individualDetails.getLastName()),
            () -> assertEquals("channelType", individualDetails.getPreferredHearingChannel()),
            () -> assertEquals("english", individualDetails.getInterpreterLanguage()),
            () -> assertEquals(true, individualDetails.getVulnerableFlag()),
            () -> assertEquals("details", individualDetails.getVulnerabilityDetails()),
            () -> assertEquals("01234567890", individualDetails.getHearingChannelPhone().get(0)),
            () -> assertEquals("hearing.channel@email.com", individualDetails.getHearingChannelEmail().get(0)),
            () -> assertEquals("First reason", individualDetails.getReasonableAdjustments().get(0)),
            () -> assertEquals("custodyStatus", individualDetails.getCustodyStatus()),
            () -> assertEquals("otherReason", individualDetails.getOtherReasonableAdjustmentDetails()),
            () -> assertRelatedParties(individualDetails.getRelatedParties())
        );
    }

    private void assertRelatedParties(List<RelatedParty> relatedParties) {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("P1");
        relatedParty1.setRelationshipType("A");

        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("P2");
        relatedParty2.setRelationshipType("B");

        assertTrue(relatedParties.containsAll(List.of(relatedParty1, relatedParty2)));
    }

    private void assertPanelRequirements(PanelRequirements panelRequirements) {
        assertAll(
            () -> assertEquals(Arrays.asList("RoleType1"), panelRequirements.getRoleType()),
            () -> assertEquals(Arrays.asList("Specialism 1"), panelRequirements.getPanelSpecialisms()),
            () -> assertEquals(Arrays.asList("AuthorisationType1"), panelRequirements.getAuthorisationTypes()),
            () -> assertEquals(Arrays.asList("AuthorisationSubType2"), panelRequirements.getAuthorisationSubType()),
            () -> assertEquals("judge1", panelRequirements.getPanelPreferences().get(0).getMemberID())
        );
    }

    private void assertFacility(List<String> facilityType) {
        assertAll(
            () -> assertEquals("RoleType1", facilityType.get(0))
        );
    }

    private void assertHearingLocation(HearingLocation hearingLocation) {
        assertAll(
            () -> assertNull(hearingLocation.getLocationId()),
            () -> assertEquals("cluster", hearingLocation.getLocationType())
        );
    }

    private void assertHearingChannels(List<String> hearingChannel) {
        assertAll(
            () -> assertEquals(2, hearingChannel.size()),
            () -> assertEquals("someChannelType", hearingChannel.get(0)),
            () -> assertEquals("someOtherChannelType", hearingChannel.get(1))
        );
    }


}
