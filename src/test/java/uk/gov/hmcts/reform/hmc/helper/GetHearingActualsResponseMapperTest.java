package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.LISTED;

class GetHearingActualsResponseMapperTest {

    @Test
    void toHearingsResponseWhenDataIsPresentForOrgDetails() {
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
                         .getActualDayParties().get(0).getActualOrganisationDetails().get(0).getName());
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
     void  checkHearingStatusWhenStatusIsHearingRequested() {
        assertEquals("HEARING_REQUESTED", getHearingActualResponse(HEARING_REQUESTED.name()).getHmcStatus());
    }

    @Test
    void checkHearingStatusWhenStatusIsHearingListed() {
        HearingActualResponse response = getHearingActualResponse(LISTED.name());

        assertEquals("AWAITING_ACTUALS", response.getHmcStatus());
    }

    private HearingActualResponse getHearingActualResponse(String status) {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        return getHearingsResponseMapper.toHearingActualResponse(
                TestingUtil.getHearingsEntityForHearingActuals(status));
    }

    @Test
    void toHearingsResponseWhenDataIsPresentForIndividualDetails() {
        GetHearingActualsResponseMapper getHearingsResponseMapper = new GetHearingActualsResponseMapper();
        HearingActualResponse response =
            getHearingsResponseMapper.toHearingActualResponse(TestingUtil
                                                                  .getHearingsEntityForHearingActualsIndividual());
        assertCommonFields(response);
        assertEquals("mr", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().get(0).getTitle());
        assertEquals("joe", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().get(0).getFirstName());
        assertEquals("bloggs", response.getHearingPlanned().getPlannedHearingDays()
            .get(0).getParties().get(0).getIndividualDetails().get(0).getLastName());
        assertEquals("firstName", response.getHearingActuals().getActualHearingDays()
            .get(0).getActualDayParties().get(0).getActualIndividualDetails().get(0).getFirstName());
        assertEquals("lastName", response.getHearingActuals().getActualHearingDays()
            .get(0).getActualDayParties().get(0).getActualIndividualDetails().get(0).getLastName());
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
        assertEquals(1,
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
}
