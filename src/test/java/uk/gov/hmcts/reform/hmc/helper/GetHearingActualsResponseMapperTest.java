package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualDayParty;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualHearingDays;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.Party;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.PauseDateTimes;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.PlannedHearingDays;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.LISTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.UPDATE_SUBMITTED;

class GetHearingActualsResponseMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(GetHearingActualsResponseMapperTest.class);

    @Test
    void toHearingsResponseWhenDataIsPresentForOrgDetails()  {
        HearingActualResponse response = getHearingActualResponse(HEARING_REQUESTED.name());
        assertCommonFields(response);
        assertEquals("name",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties()
                         .get(0).getOrganisationDetails().getName());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties()
                         .get(0).getOrganisationDetails().getCftOrganisationID());
        assertEquals("partyOrgName",
                     response.getHearingActuals().getActualHearingDays().get(0)
                         .getActualDayParties().get(0).getActualOrganisationName());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentForOrgDetails1()  {
        HearingActualResponse response = getHearingActualResponse(UPDATE_SUBMITTED.name());
        assertEquals("name",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties()
                         .get(0).getOrganisationDetails().getName());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties()
                         .get(0).getOrganisationDetails().getCftOrganisationID());
        assertEquals("partyOrgName",
                     response.getHearingActuals().getActualHearingDays().get(0)
                         .getActualDayParties().get(0).getActualOrganisationName());
    }

    @Test
    void hearingResponsePartyDetailsOnOneMatchOnAttendeeDetailsAndHearingParty() {
        HearingActualResponse response = getHearingActualResponse(HEARING_REQUESTED.name());

        assertEquals(3, response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().size());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0).getPartyID());
        assertEquals("partySubChannelA",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0)
                         .getPartyChannelSubType());
        assertEquals("role",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0).getPartyRole());
        assertEquals("name",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0)
                         .getOrganisationDetails().getName());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0)
                         .getOrganisationDetails().getCftOrganisationID());
        assertEquals(PartyType.ORG.label,
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0).getPartyType());

    }

    @Test
    void hearingResponsePartyDetailsOnMultipleMatchesOnAttendeeDetailsAndHearingParty() {
        HearingActualResponse response = getHearingActualResponse(HEARING_REQUESTED.name());

        assertEquals(3, response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().size());
        assertEquals("reference2",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(1).getPartyID());
        assertEquals("partySubChannelB",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(1)
                         .getPartyChannelSubType());
        assertEquals("role",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(1).getPartyRole());
        assertEquals("name",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(1)
                         .getOrganisationDetails().getName());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(1)
                         .getOrganisationDetails().getCftOrganisationID());

    }

    @Test
    void hearingResponsePartyDetailsOnZeroMatchOnAttendeeDetailsAndHearingParty() {
        HearingActualResponse response = getHearingActualResponse(HEARING_REQUESTED.name());

        assertEquals(3, response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().size());
        assertEquals("reference3",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(2).getPartyID());
        assertEquals("partySubChannelC",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(2)
                         .getPartyChannelSubType());
        assertNull(response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(2).getPartyRole());
        assertNull(response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(2)
                         .getOrganisationDetails());
        assertNull(response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(2)
                       .getIndividualDetails());

    }

    @Test
    void checkHearingStatusWhenStatusIsHearingRequested() {
        HearingActualResponse response = getHearingActualResponse(HEARING_REQUESTED.name());

        assertEquals("HEARING_REQUESTED", response.getHmcStatus());
    }

    @Test
    void checkHearingStatusWhenStatusIsHearingListed() {
        HearingActualResponse response = getHearingActualResponse(LISTED.name());

        assertEquals("AWAITING_ACTUALS", response.getHmcStatus());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentForIndividualDetails() {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(TestingUtil
                                                                  .getHearingsEntityForHearingActualsIndividual());
        assertCommonFields(response);
        assertEquals("mr", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().getTitle());
        assertEquals("joe", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().getFirstName());
        assertEquals("bloggs", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().getLastName());
        assertEquals("firstName", response.getHearingActuals().getActualHearingDays()
            .get(0).getActualDayParties().get(0).getActualIndividualDetails().getFirstName());
        assertEquals("lastName", response.getHearingActuals().getActualHearingDays()
            .get(0).getActualDayParties().get(0).getActualIndividualDetails().getLastName());
    }

    @Test
    void checkSortOrders()  {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(TestingUtil.getHearingsEntityForSortOrderCheck());

        List<ActualHearingDays> actualHearingDays = response.getHearingActuals().getActualHearingDays();

        assertActualHearingDays_isSorted(actualHearingDays);
        assertActualHearingDayPausesAndParties_areSorted(actualHearingDays);

        List<PlannedHearingDays> plannedHearingDaysList = response.getHearingPlanned().getPlannedHearingDays();
        assertHearingParties_isSorted(plannedHearingDaysList);
        assertHearingDayDetails_isSorted(plannedHearingDaysList);
    }

    @Test
    void actualHearingDaysList_SortedByHearingDateAscending() {

        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(TestingUtil.getHearingsEntityForSortOrderCheck());

        List<ActualHearingDays> actualHearingDays = response.getHearingActuals().getActualHearingDays();

        assertActualHearingDays_isSorted(actualHearingDays);
    }

    @Test
    void actualHearingDaysList_EmptyList() {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingEntity hearingEntity = TestingUtil.getHearingsEntityForHearingActuals(
            HearingStatus.HEARING_REQUESTED.name());
        hearingEntity.getLatestHearingResponse().get().getActualHearingEntity().setActualHearingDay(new ArrayList<>());

        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(hearingEntity);

        List<ActualHearingDays> actualHearingDays = response.getHearingActuals().getActualHearingDays();
        assertThat(actualHearingDays).isEmpty();
    }

    @Test
    void actualHearingDaysList_SingleElement() {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingEntity hearingEntity = TestingUtil.getHearingsEntityForHearingActuals(
            HearingStatus.HEARING_REQUESTED.name());
        ActualHearingDayEntity actualHearingDay = TestingUtil.createActualHearingDayEntity(
            LocalDate.now().plusMonths(1), LocalDateTime.now().plusDays(31));
        hearingEntity.getLatestHearingResponse().get().getActualHearingEntity()
            .setActualHearingDay(List.of(actualHearingDay));

        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(hearingEntity);

        List<ActualHearingDays> actualHearingDays = response.getHearingActuals().getActualHearingDays();
        assertThat(actualHearingDays).size().isEqualTo(1);
    }

    private HearingActualResponse getHearingActualResponse(String status) {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        return getHearingsResponseMapper.toHearingActualResponse(
            TestingUtil.getHearingsEntityForHearingActuals(status));
    }

    private void assertCommonFields(HearingActualResponse response) {
        assertEquals("HEARING_REQUESTED", response.getHmcStatus());
        assertEquals("serviceCode", response.getCaseDetails().getHmctsServiceCode());
        assertEquals("caseRef", response.getCaseDetails().getCaseRef());
        assertEquals("extCaseRef", response.getCaseDetails().getExternalCaseReference());
        assertEquals("contextPath", response.getCaseDetails().getCaseDeepLink());
        assertEquals("caseName", response.getCaseDetails().getHmctsInternalCaseName());
        assertEquals("publicCaseName", response.getCaseDetails().getPublicCaseName());
        assertEquals(true, response.getCaseDetails().getCaseAdditionalSecurityFlag());
        assertEquals(true, response.getCaseDetails().getCaseInterpreterRequiredFlag());
        assertEquals("locationId", response.getCaseDetails().getCaseManagementLocationCode());
        assertEquals(true, response.getCaseDetails().getCaseRestrictedFlag());
        assertEquals(LocalDate.of(2000,01,01), response.getCaseDetails().getCaseSlaStartDate());

        assertEquals("caseType", response.getCaseDetails().getCaseCategories().get(0).getCategoryType());
        assertEquals("PROBATE", response.getCaseDetails().getCaseCategories().get(0).getCategoryValue());

        assertEquals("hearingType", response.getHearingPlanned().getPlannedHearingType());
        assertEquals(LocalDateTime.parse("2000-08-10T12:20:00"),
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getPlannedStartTime());
        assertEquals(LocalDateTime.parse("2000-08-10T12:20:00"),
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getPlannedEndTime());
        assertEquals("reference",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0).getPartyID());
        assertEquals("role",
                     response.getHearingPlanned().getPlannedHearingDays().get(0).getParties().get(0).getPartyRole());

        assertEquals("hearingType",
                     response.getHearingActuals().getHearingOutcome().getHearingType());
        assertEquals(true,
                     response.getHearingActuals().getHearingOutcome().getHearingFinalFlag());
        assertEquals(HearingResultType.ADJOURNED,
                     response.getHearingActuals().getHearingOutcome().getHearingResult());
        assertEquals("resultReason",
                     response.getHearingActuals().getHearingOutcome().getHearingResultReasonType());
        assertEquals(LocalDate.of(2000,01,01),
                     response.getHearingActuals().getHearingOutcome().getHearingResultDate());
        assertEquals(LocalDate.of(2000,01,01),
                     response.getHearingActuals().getActualHearingDays().get(0).getHearingDate());
        assertEquals(LocalDateTime.parse("2021-08-10T12:20:00"),
                     response.getHearingActuals().getActualHearingDays().get(0).getHearingStartTime());
        assertEquals(LocalDateTime.parse("2021-08-10T12:20:00"),
                     response.getHearingActuals().getActualHearingDays().get(0).getHearingEndTime());
        assertEquals(LocalDateTime.parse("2021-08-10T12:20:00"),
                     response.getHearingActuals().getActualHearingDays().get(0).getPauseDateTimes()
                         .get(0).getPauseStartTime());
        assertEquals(LocalDateTime.parse("2021-08-10T12:20:00"),
                     response.getHearingActuals().getActualHearingDays().get(0).getPauseDateTimes()
                         .get(0).getPauseEndTime());
        assertEquals("1",
                     response.getHearingActuals().getActualHearingDays().get(0).getActualDayParties()
                         .get(0).getActualPartyId());
        assertEquals("roleType", response.getHearingActuals()
            .getActualHearingDays().get(0).getActualDayParties().get(0).getPartyRole());
        assertEquals("partySubChannel", response.getHearingActuals()
            .getActualHearingDays().get(0).getActualDayParties().get(0).getPartyChannelSubType());
        assertEquals(false, response.getHearingActuals()
            .getActualHearingDays().get(0).getActualDayParties().get(0).getDidNotAttendFlag());
        assertEquals("1", response.getHearingActuals()
            .getActualHearingDays().get(0).getActualDayParties().get(0).getRepresentedParty());
    }

    private void assertActualHearingDays_isSorted(List<ActualHearingDays> actualHearingDaysList) {
        logger.debug("assertActualHearingDays_isSorted...");
        boolean answer = isSortedByHearingDateAscending(actualHearingDaysList);
        assertThat(answer).isTrue();
        logger.debug("assertActualHearingDays_isSorted == {}", answer);
    }

    private void assertActualHearingDayPausesAndParties_areSorted(List<ActualHearingDays> actualHearingDaysList) {
        logger.debug("assertActualHearingDayPausesAndParties_areSorted...");
        actualHearingDaysList.forEach(e -> {
            boolean answer = isSortedPauseDateTimeAscending(e.getPauseDateTimes());
            assertThat(answer).isTrue();

            answer = isSortedByRoleTypeAndPartyId(e.getActualDayParties());
            assertThat(answer).isTrue();
        });
    }

    private void assertHearingParties_isSorted(List<PlannedHearingDays> plannedHearingDaysList) {
        logger.debug("assertHearingParties_isSorted...");
        for (PlannedHearingDays plannedHearingDays : plannedHearingDaysList) {
            assertThat(isSortedByRoleChannelAndId(plannedHearingDays.getParties())).isTrue();
        }
    }

    private void assertHearingDayDetails_isSorted(List<PlannedHearingDays> plannedHearingDaysList) {
        logger.debug("assertHearingDayDetails_isSorted...");
        assertThat(isSortedByPlannedHearingDateAscending(plannedHearingDaysList)).isTrue();
    }

    private boolean isSortedByRoleTypeAndPartyId(List<ActualDayParty> actualDayPartyList) {
        logger.debug("isSortedByRoleTypeAndPartyId...");
        boolean answer = true;
        for (int i = 0; i < actualDayPartyList.size() - 1; i++) {
            ActualDayParty current = actualDayPartyList.get(i);
            ActualDayParty next = actualDayPartyList.get(i + 1);
            logger.debug("current:{}/{} vs next:{}/{}",
                         current.getPartyRole(), current.getActualPartyId(),
                         next.getPartyRole(), next.getActualPartyId());

            int roleComparison = current.getPartyRole().compareTo(next.getPartyRole());
            if (roleComparison > 0
                || (roleComparison == 0 && current.getActualPartyId().compareTo(next.getActualPartyId()) > 0)) {
                answer = false;
                break;
            }
        }
        logger.debug("isSortedByRoleTypeAndPartyId = {}", answer);
        return answer;
    }

    private boolean isSortedByHearingDateAscending(List<ActualHearingDays> actualHearingDaysList) {
        logger.debug("isSortedByHearingDateAscending...");
        boolean answer = true;
        for (int i = 0; i < actualHearingDaysList.size() - 1; i++) {
            logger.debug("current:{} vs next:{}",
                         actualHearingDaysList.get(i).getHearingDate(),
                         actualHearingDaysList.get(i + 1).getHearingDate());
            if (actualHearingDaysList.get(i).getHearingDate()
                .isAfter(actualHearingDaysList.get(i + 1).getHearingDate())) {
                answer = false;
                break;
            }
        }
        logger.debug("isSortedByHearingDateAscending = {}", answer);
        return answer;
    }

    private boolean isSortedByPlannedHearingDateAscending(List<PlannedHearingDays> plannedHearingDaysList) {
        logger.debug("isSortedByPlannedHearingDateAscending...");
        boolean answer = true;
        for (int i = 0; i < plannedHearingDaysList.size() - 1; i++) {
            logger.debug("current:{} vs next:{}",
                         plannedHearingDaysList.get(i).getPlannedStartTime(),
                         plannedHearingDaysList.get(i + 1).getPlannedStartTime());
            if (plannedHearingDaysList.get(i).getPlannedStartTime()
                .isAfter(plannedHearingDaysList.get(i + 1).getPlannedStartTime())) {
                answer = false;
                break;
            }
        }
        logger.debug("isSortedByPlannedHearingDateAscending = {}", answer);
        return answer;
    }

    private boolean isSortedPauseDateTimeAscending(List<PauseDateTimes> pauseDateTimes) {
        logger.debug("isSortedPauseDateTimeAscending...");
        boolean answer = true;
        for (int i = 0; i < pauseDateTimes.size() - 1; i++) {
            logger.debug("current:{} vs next:{}",
                         pauseDateTimes.get(i).getPauseStartTime(),
                         pauseDateTimes.get(i + 1).getPauseStartTime());
            if (pauseDateTimes.get(i).getPauseStartTime().isAfter(pauseDateTimes.get(i + 1).getPauseStartTime())) {
                answer = false;
                break;
            }
        }
        logger.debug("isSortedPauseDateTimeAscending = {}", answer);
        return answer;
    }

    private boolean isSortedByRoleChannelAndId(List<Party> parties) {
        logger.debug("isSortedByRoleChannelAndId...");
        boolean answer = true;
        for (int i = 0; i < parties.size() - 1; i++) {
            Party current = parties.get(i);
            Party next = parties.get(i + 1);
            logger.debug("current:{}/{}/{} vs next:{}/{}/{}",
                         current.getPartyRole(), current.getPartyChannelSubType(), current.getPartyID(),
                         next.getPartyRole(), next.getPartyChannelSubType(), next.getPartyID());

            int roleComparison = Objects.compare(current.getPartyRole(), next.getPartyRole(),
                                                 Comparator.nullsLast(String::compareTo));
            if (roleComparison > 0) {
                return false;
            } else if (roleComparison == 0) {
                int channelComparison = Objects.compare(current.getPartyChannelSubType(), next.getPartyChannelSubType(),
                                                        Comparator.nullsLast(String::compareTo));
                if (channelComparison > 0) {
                    return false;
                } else if (channelComparison == 0 && current.getPartyID().compareTo(next.getPartyID()) > 0) {
                    answer = false;
                    break;
                }
            }
        }
        logger.debug("isSortedByRoleChannelAndId = {}", answer);
        return answer;
    }

}
