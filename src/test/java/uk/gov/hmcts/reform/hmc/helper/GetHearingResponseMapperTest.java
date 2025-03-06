package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GetHearingResponseMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(GetHearingResponseMapperTest.class);

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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.IND.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
        assertPartyDetails(response.getPartyDetails().get(0), PartyType.ORG.getLabel());
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
    void toHearingsResponseWhenCancellationReasonsIsEmpty() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertNull(response.getRequestDetails().getCancellationReasonCodes());
    }

    @Test
    void toHearingsResponseWhenCancellationReasonsIsPresent() {
        HearingEntity hearingEntity = TestingUtil.getCaseHearingsEntity(PartyType.ORG);
        List<CancellationReasonsEntity> cancelReasons = new ArrayList<>();
        CancellationReasonsEntity cancellationReason1 = new CancellationReasonsEntity();
        cancellationReason1.setCancellationReasonType("ReasonType");
        cancelReasons.add(cancellationReason1);
        hearingEntity.getCaseHearingRequests().get(0).setCancellationReasons(cancelReasons);
        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);
        assertEquals("ReasonType", response.getRequestDetails().getCancellationReasonCodes().get(0));
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

    @Test
    void listsAreSortedCorrectly() {
        HearingEntity hearingEntity = TestingUtil.createHearingEntity();

        GetHearingResponse response = getHearingResponseMapper.toHearingResponse(hearingEntity);

        List<HearingDaySchedule> schedules = response.getHearingResponse().getHearingDaySchedule();
        assertThat(isSortedByStartDateTimeAndHearingJudgeId(schedules)).isTrue();

        List<LocalDateTime> startDateTimes = new ArrayList<>();

        if (null != schedules && !schedules.isEmpty()) {
            for (HearingDaySchedule schedule : schedules) {
                startDateTimes.add(schedule.getHearingStartDateTime());

                List<Attendee> attendees = schedule.getAttendees();
                List<String> partyIds = new ArrayList<>();

                if (null != attendees && !attendees.isEmpty()) {
                    for (Attendee attendee : attendees) {
                        partyIds.add(attendee.getPartyId());
                    }
                }
                assertThat(isAscendingOrder("partyId", partyIds)).isTrue();
            }
        }
        assertThat(isAscendingLocalDateTimeOrder("startDateTime", startDateTimes)).isTrue();
    }

    private boolean isSortedByStartDateTimeAndHearingJudgeId(List<HearingDaySchedule> scheduleList) {
        for (int i = 0; i < scheduleList.size() - 1; i++) {
            HearingDaySchedule current = scheduleList.get(i);
            HearingDaySchedule next = scheduleList.get(i + 1);
            if (current.getHearingStartDateTime().isAfter(next.getHearingStartDateTime())
                || (current.getHearingStartDateTime().isEqual(next.getHearingStartDateTime())
                && current.getHearingJudgeId().compareTo(next.getHearingJudgeId()) > 0)) {
                return false;
            }
        }
        return true;
    }


    @Test
    void setHearingJudgeAndPanelMemberIds_SetsJudgeAndPanelMembersCorrectly() {
        HearingDayDetailsEntity hearingDayDetails = TestingUtil.createHearingDayDetailsEntity(1,
                                                   LocalDateTime.of(2025,9,1,10,0,0));

        List<HearingDayPanelEntity> panelEntities = new ArrayList<>();
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(1, "judge1", true, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(2, "panel5", false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(3, "panel1", false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(4, "panel4", false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(5, "panel2", false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(6, "panel3", false, hearingDayDetails));

        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        getHearingResponseMapper.setHearingJudgeAndPanelMemberIds(panelEntities, hearingDaySchedule);

        assertEquals("judge1", hearingDaySchedule.getHearingJudgeId());
        assertThat(hearingDaySchedule.getPanelMemberIds())
            .isEqualTo(Arrays.asList("panel1", "panel2","panel3", "panel4","panel5"));
    }

    @Test
    void setHearingJudgeAndPanelMemberIds_SetsPanelMembersWhenNoJudge() {
        HearingDayDetailsEntity hearingDayDetails = TestingUtil.createHearingDayDetailsEntity(1,
                                                 LocalDateTime.of(2025,9,1,10,0,0));

        List<HearingDayPanelEntity> panelEntities = new ArrayList<>();
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(1, "panel1", false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(2, "panel2", false, hearingDayDetails));

        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        getHearingResponseMapper.setHearingJudgeAndPanelMemberIds(panelEntities, hearingDaySchedule);

        assertNull(hearingDaySchedule.getHearingJudgeId());
        assertEquals(Arrays.asList("panel1", "panel2"), hearingDaySchedule.getPanelMemberIds());
    }

    @Test
    void setHearingJudgeAndPanelMemberIds_SetsJudgeWhenNoPanelMembers() {
        HearingDayDetailsEntity hearingDayDetails = TestingUtil.createHearingDayDetailsEntity(1,
                                                 LocalDateTime.of(2025,9,1,10,0,0));

        List<HearingDayPanelEntity> panelEntities = new ArrayList<>();
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(1, "judge1", true, hearingDayDetails));

        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        getHearingResponseMapper.setHearingJudgeAndPanelMemberIds(panelEntities, hearingDaySchedule);

        assertEquals("judge1", hearingDaySchedule.getHearingJudgeId());
        assertTrue(hearingDaySchedule.getPanelMemberIds().isEmpty());
    }

    @Test
    void setHearingJudgeAndPanelMemberIds_HandlesNullPanelUserId() {
        HearingDayDetailsEntity hearingDayDetails = TestingUtil.createHearingDayDetailsEntity(1,
                                                 LocalDateTime.of(2025,9,1,10,0,0));

        List<HearingDayPanelEntity> panelEntities = new ArrayList<>();
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(1, null, false, hearingDayDetails));
        panelEntities.add(TestingUtil.createHearingDayPanelEntity(1, "panel1", false, hearingDayDetails));

        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        getHearingResponseMapper.setHearingJudgeAndPanelMemberIds(panelEntities, hearingDaySchedule);

        assertNull(hearingDaySchedule.getHearingJudgeId());
        assertEquals(Arrays.asList("panel1", null), hearingDaySchedule.getPanelMemberIds());
    }

    @Test
    void setHearingJudgeAndPanelMemberIds_HandlesEmptyList() {
        List<HearingDayPanelEntity> panelEntities = new ArrayList<>();

        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        getHearingResponseMapper.setHearingJudgeAndPanelMemberIds(panelEntities, hearingDaySchedule);

        assertNull(hearingDaySchedule.getHearingJudgeId());
        assertTrue(hearingDaySchedule.getPanelMemberIds().isEmpty());
    }

    private static <T extends Comparable<T>> boolean isAscendingOrder(String listName, List<T> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            logger.debug("{} {}:{} - {}:{})", listName, i, (i + 1), list.get(i), list.get(i + 1));
            if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAscendingLocalDateTimeOrder(String listName, List<LocalDateTime> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            logger.debug("{} {}:{} - {}:{})", listName, i, (i + 1), list.get(i), list.get(i + 1));
            if (list.get(i).isAfter(list.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    private void assertRequestDetails(RequestDetails requestDetails) {
        assertAll(
            () -> assertEquals("HEARING_REQUESTED", requestDetails.getStatus()),
            () -> assertEquals(1, requestDetails.getVersionNumber())
        );
    }

    private void assertCaseDetails(CaseDetails caseDetails) {
        assertAll(
            () -> assertEquals("TEST", caseDetails.getHmctsServiceCode()),
            () -> assertEquals("12345", caseDetails.getCaseRef())
        );
    }

    private void assertHearingDetails(HearingDetails hearingDetails) {
        assertAll(
            () -> assertEquals("Some hearing type", hearingDetails.getHearingType()),
            () -> assertEquals(
                ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel(), hearingDetails.getListingAutoChangeReasonCode())
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
            () -> assertEquals("Cancelled Reason 1", hearingResponse.getHearingCancellationReason()),
            () -> assertEquals(10, hearingResponse.getRequestVersion())
        );
    }

    private void assertHearingDaySchedule(HearingDaySchedule hearingDaySchedule) {
        assertAll(
            () -> assertEquals("venue1", hearingDaySchedule.getHearingVenueId()),
            () -> assertEquals("room1", hearingDaySchedule.getHearingRoomId()),
            () -> assertEquals("PanelUser1", hearingDaySchedule.getPanelMemberIds().get(0))
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
