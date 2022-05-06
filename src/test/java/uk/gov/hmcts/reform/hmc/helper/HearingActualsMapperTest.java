package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayParties;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPartyDetail;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPauseDayTime;
import uk.gov.hmcts.reform.hmc.model.ActualHearingOrganisationDetail;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingActualsOutcome;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingActualsMapperTest {


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processHearingRequestToEntityForOrg() {
        ActualHearingOrganisationDetail organisationDetail = new ActualHearingOrganisationDetail();
        organisationDetail.setName("name");
        ActualHearingDayPauseDayTime hearingDayPauseDayTime = TestingUtil.getHearingActualDayPause(
            LocalDateTime.of(2022, 01, 28, 10, 00),
            LocalDateTime.of(2022, 01, 28, 12, 00)
        );
        ActualHearingDayParties hearingDayParty = TestingUtil.getHearingActualDayParties(
            "1",
            "RoleType1",
            null,
            organisationDetail,
            "SubType1",
            false,
            null
        );
        HearingActual hearingActual = getHearingActual(null, organisationDetail,
                                                       "2", List.of(hearingDayPauseDayTime),
                                                       List.of(hearingDayParty)
        );

        HearingActualsMapper actualsMapper = new HearingActualsMapper();
        ActualHearingEntity response = actualsMapper.toActualHearingEntity(hearingActual);

        assertCommonFields(response);
        assertEquals(
            "name",
            response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                .getActualAttendeeIndividualDetail().get(0).getPartyOrganisationName()
        );
        assertNull(response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                       .getActualAttendeeIndividualDetail().get(0).getFirstName());
        assertNull(response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                       .getActualAttendeeIndividualDetail().get(0).getLastName());
    }

    @Test
    void processHearingRequestToEntityForInd() {
        ActualHearingDayPartyDetail individualDetails = new ActualHearingDayPartyDetail();
        individualDetails.setFirstName("fname");
        individualDetails.setLastName("lname");
        ActualHearingDayPauseDayTime hearingDayPauseDayTime = TestingUtil.getHearingActualDayPause(
            LocalDateTime.of(2022, 01, 28, 10, 00),
            LocalDateTime.of(2022, 01, 28, 12, 00)
        );
        ActualHearingDayParties hearingDayParty = TestingUtil.getHearingActualDayParties(
            "1",
            "RoleType1",
            individualDetails,
            null,
            "SubType1",
            false,
            null
        );
        HearingActual hearingActual = getHearingActual(individualDetails, null,
                                                       "2", List.of(hearingDayPauseDayTime),
                                                       List.of(hearingDayParty)
        );

        HearingActualsMapper actualsMapper = new HearingActualsMapper();
        ActualHearingEntity response = actualsMapper.toActualHearingEntity(hearingActual);

        assertCommonFields(response);
        assertNull(response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                       .getActualAttendeeIndividualDetail().get(0).getPartyOrganisationName());
        assertEquals(
            "fname",
            response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                .getActualAttendeeIndividualDetail().get(0).getFirstName()
        );
        assertEquals(
            "lname",
            response.getActualHearingDay().get(0).getActualHearingParty().get(0)
                .getActualAttendeeIndividualDetail().get(0).getLastName()
        );
    }

    @Test
    void processHearingRequestToEntityForIndWhenPauseTimesIsNull() {
        ActualHearingDayPartyDetail individualDetails = new ActualHearingDayPartyDetail();
        individualDetails.setFirstName("fname");
        individualDetails.setLastName("lname");
        ActualHearingDayParties hearingDayParty = TestingUtil.getHearingActualDayParties(
            "1",
            "RoleType1",
            individualDetails,
            null,
            "SubType1",
            false,
            null
        );

        HearingActual hearingActual = getHearingActual(individualDetails, null,
                                                       "2", null,
                                                       List.of(hearingDayParty)
        );

        HearingActualsMapper actualsMapper = new HearingActualsMapper();
        ActualHearingEntity response = actualsMapper.toActualHearingEntity(hearingActual);

        assertEquals(0, response.getActualHearingDay().get(0).getActualHearingDayPauses().size());
    }

    @Test
    void processHearingRequestToEntityForIndWhenActualHearingPartyEntitiesIsNull() {
        ActualHearingDayPartyDetail individualDetails = new ActualHearingDayPartyDetail();
        individualDetails.setFirstName("fname");
        individualDetails.setLastName("lname");

        HearingActual hearingActual = getHearingActual(individualDetails, null,
                                                       "2", null, null
        );

        HearingActualsMapper actualsMapper = new HearingActualsMapper();
        ActualHearingEntity response = actualsMapper.toActualHearingEntity(hearingActual);
        assertEquals(0, response.getActualHearingDay().get(0).getActualHearingParty().size());
    }

    @Test
    void shouldThrowBadRequestForNotFoundPartyId() {
        ActualHearingDayPartyDetail individualDetails = new ActualHearingDayPartyDetail();
        individualDetails.setFirstName("fname");
        individualDetails.setLastName("lname");
        ActualHearingDayPauseDayTime hearingDayPauseDayTime1 = TestingUtil.getHearingActualDayPause(
            LocalDateTime.of(2022, 01, 28, 10, 00),
            LocalDateTime.of(2022, 01, 28, 12, 00)
        );
        ActualHearingDayParties hearingDayParty = TestingUtil.getHearingActualDayParties(
            "1",
            "RoleType1",
            individualDetails,
            null,
            "SubType1",
            false,
            null
        );
        HearingActual hearingActual = getHearingActual(individualDetails, null,
                                                       "10", List.of(hearingDayPauseDayTime1),
                                                       List.of(hearingDayParty)
        );

        HearingActualsMapper actualsMapper = new HearingActualsMapper();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            actualsMapper.toActualHearingEntity(hearingActual);
        });
        assertEquals("Cannot find unique PartyID with value 10", exception.getMessage());
    }

    private void assertCommonFields(ActualHearingEntity response) {
        assertEquals("witness hearing", response.getActualHearingType());
        assertTrue(response.getActualHearingIsFinalFlag());
        assertEquals(HearingResultType.COMPLETED, response.getHearingResultType());
        assertEquals("hearing Reason", response.getHearingResultReasonType());
        assertEquals(LocalDate.of(2022, 02, 01), response.getHearingResultDate());

        assertEquals(2, response.getActualHearingDay().size());
        assertEquals(LocalDate.of(2022, 01, 28),
                     response.getActualHearingDay().get(0).getHearingDate());
        assertEquals(LocalDateTime.of(2022, 01, 28, 10, 00),
                     response.getActualHearingDay().get(0).getStartDateTime());
        assertEquals(LocalDateTime.of(2022, 01, 28, 15, 00),
                     response.getActualHearingDay().get(0).getEndDateTime());
        assertEquals(1, response.getActualHearingDay().get(0).getActualHearingParty().size());
        assertEquals("1", response.getActualHearingDay().get(0).getActualHearingParty().get(0).getPartyId());
        assertEquals(
            "RoleType1",
            response.getActualHearingDay().get(0).getActualHearingParty().get(0).getActualPartyRoleType()
        );
        assertFalse(response.getActualHearingDay().get(0).getActualHearingParty().get(0).getDidNotAttendFlag());
        assertEquals(1, response.getActualHearingDay().get(0)
                       .getActualHearingParty().get(0).getActualHearingDay().getActualHearingDayPauses().size());
        assertEquals(
            LocalDateTime.of(2022, 01, 28, 10, 00),
            response.getActualHearingDay().get(0).getActualHearingDayPauses().get(0).getPauseDateTime()
        );
        assertEquals(
            LocalDateTime.of(2022, 01, 28, 12, 00),
            response.getActualHearingDay().get(0).getActualHearingDayPauses().get(0).getResumeDateTime()
        );

        assertEquals(LocalDate.of(2022, 01, 29),
                     response.getActualHearingDay().get(1).getHearingDate());
        assertEquals(LocalDateTime.of(2022, 01, 29, 10, 00),
                     response.getActualHearingDay().get(1).getStartDateTime());
        assertEquals(LocalDateTime.of(2022, 01, 29, 17, 30),
                     response.getActualHearingDay().get(1).getEndDateTime());

        assertTrue(response.getActualHearingDay().get(1).getActualHearingParty().get(0)
                       .getActualHearingDay().getActualHearingDayPauses().size() == 2);
        assertEquals(
            LocalDateTime.of(2022, 01, 29, 12, 00),
            response.getActualHearingDay().get(1).getActualHearingDayPauses().get(0).getPauseDateTime()
        );
        assertEquals(
            LocalDateTime.of(2022, 01, 29, 12, 30),
            response.getActualHearingDay().get(1).getActualHearingDayPauses().get(0).getResumeDateTime()
        );
        assertEquals(
            LocalDateTime.of(2022, 01, 29, 15, 00),
            response.getActualHearingDay().get(1).getActualHearingDayPauses().get(1).getPauseDateTime()
        );
        assertEquals(
            LocalDateTime.of(2022, 01, 29, 15, 30),
            response.getActualHearingDay().get(1).getActualHearingDayPauses().get(1).getResumeDateTime()
        );

        assertEquals(3, response.getActualHearingDay().get(1).getActualHearingParty().size());

        assertEquals("1", response.getActualHearingDay().get(1).getActualHearingParty().get(0).getPartyId());
        assertEquals(
            "RoleType1",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualPartyRoleType()
        );
        assertTrue(response.getActualHearingDay().get(1).getActualHearingParty().get(0).getDidNotAttendFlag());
        assertEquals(
            "2",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualPartyRelationshipDetail()
                .get(0).getTargetActualPartyId().getPartyId()
        );
        assertEquals(
            "1",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualPartyRelationshipDetail()
                .get(0).getSourceActualPartyId().getPartyId()
        );
        assertEquals(
            "TestFirstName",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualAttendeeIndividualDetail()
                .get(0).getFirstName()
        );
        assertEquals(
            "TestLastName",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualAttendeeIndividualDetail()
                .get(

                    0).getLastName()
        );
        assertEquals(
            "SubType2",
            response.getActualHearingDay().get(1).getActualHearingParty().get(0).getActualAttendeeIndividualDetail()
                .get(0).getPartyActualSubChannelType()
        );

        assertEquals("2", response.getActualHearingDay().get(1).getActualHearingParty().get(1).getPartyId());
        assertEquals(
            "RoleType2",
            response.getActualHearingDay().get(1).getActualHearingParty().get(1).getActualPartyRoleType()
        );
        assertFalse(response.getActualHearingDay().get(1).getActualHearingParty().get(1).getDidNotAttendFlag());
        assertNull(response.getActualHearingDay().get(1)
                       .getActualHearingParty().get(1).getActualPartyRelationshipDetail());
        assertEquals(
            "TestRepFirstName",
            response.getActualHearingDay().get(1).getActualHearingParty().get(1)
                .getActualAttendeeIndividualDetail().get(0).getFirstName()
        );
        assertEquals(
            "TestRepLastName",
            response.getActualHearingDay().get(1).getActualHearingParty().get(1)
                .getActualAttendeeIndividualDetail().get(0).getLastName()
        );
        assertEquals(
            "SubType2",
            response.getActualHearingDay().get(1).getActualHearingParty().get(1)
                .getActualAttendeeIndividualDetail().get(0).getPartyActualSubChannelType()
        );

        assertEquals("3", response.getActualHearingDay().get(1).getActualHearingParty().get(2).getPartyId());
        assertEquals(
            "RoleType3",
            response.getActualHearingDay().get(1).getActualHearingParty().get(2).getActualPartyRoleType()
        );
        assertFalse(response.getActualHearingDay().get(1).getActualHearingParty().get(2).getDidNotAttendFlag());
        assertNull(response.getActualHearingDay().get(1)
                       .getActualHearingParty().get(2).getActualPartyRelationshipDetail());
        assertEquals(
            "Organisation Name",
            response.getActualHearingDay().get(1).getActualHearingParty().get(2)
                .getActualAttendeeIndividualDetail().get(0).getPartyOrganisationName()
        );
        assertEquals(
            "SubType2",
            response.getActualHearingDay().get(1).getActualHearingParty().get(2)
                .getActualAttendeeIndividualDetail().get(0).getPartyActualSubChannelType()
        );
    }

    private HearingActual getHearingActual(ActualHearingDayPartyDetail individualDetails,
                                           ActualHearingOrganisationDetail organisationDetail, String partyId,
                                           List<ActualHearingDayPauseDayTime> hearingDayPauseDayTimes,
                                           List<ActualHearingDayParties> hearingDayParties) {
        ActualHearingDay actualHearingDay1 = generateHearingDay1(
            individualDetails,
            organisationDetail,
            hearingDayPauseDayTimes,
            hearingDayParties
        );
        ActualHearingDay actualHearingDay2 = generateHearingDay2(partyId);
        HearingActualsOutcome hearingOutcome =
            TestingUtil.getHearingActualOutcome(
                "witness hearing",
                true,
                "COMPLETED",
                "hearing Reason",
                LocalDate.of(2022, 02, 01)
            );
        HearingActual hearingActual = TestingUtil.getHearingActual(
            hearingOutcome,
            List.of(actualHearingDay1, actualHearingDay2)
        );
        return hearingActual;
    }

    private ActualHearingDay generateHearingDay2(String partyId) {
        ActualHearingDayPartyDetail indDetails = new ActualHearingDayPartyDetail();
        indDetails.setFirstName("TestFirstName");
        indDetails.setLastName("TestLastName");
        ActualHearingDayPartyDetail indDetails1 = new ActualHearingDayPartyDetail();
        indDetails1.setFirstName("TestRepFirstName");
        indDetails1.setLastName("TestRepLastName");
        ActualHearingOrganisationDetail orgDetail = new ActualHearingOrganisationDetail();
        orgDetail.setName("Organisation Name");
        ActualHearingDayParties hearingDayParty2 = TestingUtil.getHearingActualDayParties(
            "1",
            "RoleType1",
            indDetails,
            null,
            "SubType2",
            true,
            partyId
        );
        ActualHearingDayParties hearingDayParty3 = TestingUtil.getHearingActualDayParties(
            "2",
            "RoleType2",
            indDetails1,
            null,
            "SubType2",
            false,
            null
        );
        ActualHearingDayParties hearingDayParty4 = TestingUtil.getHearingActualDayParties(
            "3",
            "RoleType3",
            null,
            orgDetail,
            "SubType2",
            false,
            null
        );
        ActualHearingDay actualHearingDay2 =
            TestingUtil.getHearingActualDay(
                LocalDate.of(2022, 01, 29),
                LocalDateTime.of(2022, 01, 29, 10, 00),
                LocalDateTime.of(2022, 01, 29, 17, 30),
                List.of(TestingUtil.getHearingActualDayPause(
                    LocalDateTime.of(2022, 01, 29, 12, 00),
                    LocalDateTime.of(2022, 01, 29, 12, 30)
                ), TestingUtil.getHearingActualDayPause(
                    LocalDateTime.of(2022, 01, 29, 15, 00),
                    LocalDateTime.of(2022, 01, 29, 15, 30)
                )),
                List.of(hearingDayParty2, hearingDayParty3, hearingDayParty4)
            );
        return actualHearingDay2;
    }

    private ActualHearingDay generateHearingDay1(ActualHearingDayPartyDetail individualDetails,
                                                 ActualHearingOrganisationDetail organisationDetail,
                                                 List<ActualHearingDayPauseDayTime> hearingDayPauseDayTimes,
                                                 List<ActualHearingDayParties> hearingDayParties) {
        ActualHearingDay actualHearingDay1 = TestingUtil.getHearingActualDay(
            LocalDate.of(2022, 01, 28),
            LocalDateTime.of(2022, 01, 28, 10, 00),
            LocalDateTime.of(2022, 01, 28, 15, 00),
            hearingDayPauseDayTimes,
            hearingDayParties
        );
        return actualHearingDay1;
    }

}
