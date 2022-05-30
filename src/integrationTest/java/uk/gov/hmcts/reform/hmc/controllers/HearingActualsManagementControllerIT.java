package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.TestFixtures;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import wiremock.com.jayway.jsonpath.DocumentContext;
import wiremock.com.jayway.jsonpath.JsonPath;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_END_TIME_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_HEARING_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_ORGANISATION_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PARTY_CHANNEL_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PARTY_CHANNEL_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PARTY_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PARTY_ROLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PARTY_ROLE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PAUSE_END_TIME_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_PAUSE_START_TIME_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_REPRESENTED_PARTY_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_HEARING_DAY_START_TIME_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_FINAL_FLAG_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_REASON_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_REQUEST_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_TYPE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.actualHearingDay;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingActualsOutcome;

class HearingActualsManagementControllerIT extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/hearingActuals";
    private static final String INSERT_HEARING_ACTUALS = "classpath:sql/put-hearing-actuals.sql";
    private static final String INSERT_HEARING_ACTUALS1 = "classpath:sql/get-HearingsActual_request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Nested
    @DisplayName("PutHearingActuals")
    class PutHearingActuals {
        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-01
        @Test
        void shouldReturn404_WhenHearingIdDoesNotExist() throws Exception {
            mockMvc.perform(put(URL + "/2990000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem("001 No such id: 2990000001")))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsInvalid() throws Exception {
            mockMvc.perform(put(URL + "/1000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem("Invalid hearing Id")))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-02
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenHearingHasInvalidStatusOfHearingRequested() throws Exception {
            mockMvc.perform(put(URL + "/2000000000") // status HEARING_REQUESTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("002 invalid status HEARING_REQUESTED"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-03
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenHearingHasInvalidStatusOfAwaitingListing() throws Exception {
            mockMvc.perform(put(URL + "/2000000200") // status AWAITING_LISTING
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("002 invalid status AWAITING_LISTING"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-04
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenSuppliedHearingActualPayloadContainsDuplicateHearingDates() throws Exception {
            mockMvc.perform(put(URL + "/2000000100")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                    TestingUtil.hearingActualWithDuplicatedHearingDate())))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("004 non-unique dates"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-05
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenSuppliedHearingActualPayloadContainsDuplicateHearingDates1() throws Exception {
            mockMvc.perform(put(URL + "/2000000100")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                    TestingUtil.hearingActualWithHearingDateInFuture())))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("003 invalid date"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-06
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenSuppliedHearingDatesBeforeFirstPlannedHearingDate() throws Exception {
            mockMvc.perform(put(URL + "/2000000302")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                    TestingUtil.hearingActualWithHearingDates(
                                        Arrays.asList(actualHearingDay(LocalDate.of(2022, 1, 28)),
                                                      actualHearingDay(LocalDate.of(2022, 1, 29)))))))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("003 invalid date"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-80 AC-07
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenSuppliedHearingHasHearingResultOfAdjournedWithoutHearingResultReasonType()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                    TestingUtil.hearingActual(
                                        hearingActualsOutcome("ADJOURNED", null),
                                        Arrays.asList(actualHearingDay(LocalDate.of(2022, 1, 28)),
                                                      actualHearingDay(LocalDate.of(2022, 1, 29)))))))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("ADJOURNED result requires a hearingResultReasonType"))))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-08
        // https://tools.hmcts.net/jira/browse/HMAN-82 AC01
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfListed()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001000") // LISTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001000").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingType").value("Witness Statement"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingFinalFlag").value("false"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResult").value("COMPLETED"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultReasonType")
                               .value("Nothing more to hear"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultDate").value("2022-02-01"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-01-28"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingStartTime")
                               .value("2022-01-28T10:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingEndTime")
                               .value("2022-01-28T15:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseStartTime")
                               .value("2022-01-28T12:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseEndTime")
                               .value("2022-01-28T12:30:00"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].actualPartyId")
                               .value("123"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyRole")
                               .value("43333"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyChannelSubType")
                               .value("claiming party"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].didNotAttendFlag")
                               .value("false"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].representedParty")
                               .value(IsNull.nullValue()))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.lastName")
                               .value("WitnessLastName1"))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.firstName")
                               .value("WitnessForeName1"));

        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS1})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingActualsUpdate()
            throws Exception {
            mockMvc.perform(get(URL + "/2000000000").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingType").value("witness hearing"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingFinalFlag").value("true"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResult").value("COMPLETED"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultReasonType")
                               .value(IsNull.nullValue()))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultDate").value("2022-02-15"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays", hasSize(2)))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-02-05"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[1].hearingDate").value("2022-02-06"));

            mockMvc.perform(put(URL + "/2000000000") // LISTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(200))
                .andReturn();

            mockMvc.perform(get(URL + "/2000000000").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays", hasSize(3)))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-01-28"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[1].hearingDate").value("2022-01-29"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[2].hearingDate").value("2022-01-31"))

                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingType").value("Witness Statement"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingFinalFlag").value("false"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResult").value("COMPLETED"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultReasonType")
                               .value("Nothing more to hear"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultDate").value("2022-02-01"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-01-28"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingStartTime")
                               .value("2022-01-28T10:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingEndTime")
                               .value("2022-01-28T15:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseStartTime")
                               .value("2022-01-28T12:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseEndTime")
                               .value("2022-01-28T12:30:00"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].actualPartyId")
                               .value("123"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyRole")
                               .value("43333"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyChannelSubType")
                               .value("claiming party"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].didNotAttendFlag")
                               .value("false"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].representedParty")
                               .value(IsNull.nullValue()))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.lastName")
                               .value("WitnessLastName1"))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.firstName")
                               .value("WitnessForeName1"));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS1})
        void shouldReturn200_WhenActualPartyIdIsNullForHearingActualsUpdate()
            throws Exception {
            mockMvc.perform(get(URL + "/2000000000").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingType").value("witness hearing"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingFinalFlag").value("true"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResult").value("COMPLETED"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultReasonType")
                               .value(IsNull.nullValue()))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultDate").value("2022-02-15"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays", hasSize(2)))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-02-05"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[1].hearingDate").value("2022-02-06"));

            mockMvc.perform(put(URL + "/2000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN-259-ValidPayload6-actualPartyId-null.json")))
                .andExpect(status().is(200))
                .andReturn();

            mockMvc.perform(get(URL + "/2000000000").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays", hasSize(1)))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingDate").value("2022-01-28"))

                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingType").value("BBA3-DIR"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingFinalFlag").value("false"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResult").value("COMPLETED"))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultReasonType")
                               .value(""))
                .andExpect(jsonPath("$.hearingActuals.hearingOutcome.hearingResultDate").value("2022-02-01"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingStartTime")
                               .value("2022-01-28T08:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].hearingEndTime")
                               .value("2022-01-28T13:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseStartTime")
                               .value("2022-01-28T12:00:00"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].pauseDateTimes[0].pauseEndTime")
                               .value("2022-01-28T12:30:00"))

                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].actualPartyId")
                               .value("P1"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyRole")
                               .value("DEF"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyChannelSubType")
                               .value("INTER"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].didNotAttendFlag")
                               .value("false"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].representedParty")
                               .value(IsNull.nullValue()))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.lastName")
                               .value("Smith"))
                .andExpect(jsonPath(
                    "$.hearingActuals.actualHearingDays[0].actualDayParties[0].individualDetails.firstName")
                               .value("Jane"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].actualPartyId")
                          .value("P2"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyRole")
                               .value("APP"))
                .andExpect(jsonPath("$.hearingActuals.actualHearingDays[0].actualDayParties[0].partyChannelSubType")
                               .value("INTER"));
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-09
        // https://tools.hmcts.net/jira/browse/HMAN-82 AC02
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfUpdateRequested()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001100") // UPDATE_REQUESTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload2.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001100")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-10
        // https://tools.hmcts.net/jira/browse/HMAN-82 AC03
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfUpdateSubmitted()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload3-no-partyId-supplied.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001200")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-82 AC04
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadWithHearingResultAsCompleted()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload4-Completed.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001200")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HMAN-82 AC05
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadWithHearingResultAsCancelled()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload5-Cancelled.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001200")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn200_WhenSuppliedValidPayloadWithNoActualHearingDaysElementPresent()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload5-no-actualHearingDays.json")))
                .andExpect(status().is(200))
                .andReturn();
            mockMvc.perform(get(URL + "/2000001200")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-11
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenSuppliedHearingHasHearingResultOfCancelledWithoutHearingResultReasonType()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                    TestingUtil.hearingActual(
                                        hearingActualsOutcome("CANCELLED", null),
                                        Arrays.asList(actualHearingDay(LocalDate.of(2022, 1, 28)),
                                                      actualHearingDay(LocalDate.of(2022, 1, 29)))))))
                .andExpect(status().is(400))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("PutHearingActualsJsr303Validation")
    class PutHearingActualsJsr303Validation {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingOutcomeHearingType() throws Exception {
            verifyErrorOnMissingNode(HA_OUTCOME_TYPE_NOT_EMPTY,
                                     "$['hearingOutcome']['hearingType']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenHearingTypeTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_OUTCOME_TYPE_MAX_LENGTH,
                                          "$['hearingOutcome']['hearingType']",
                                          41);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingOutcomeFinalFlag() throws Exception {
            verifyErrorOnMissingNode(HA_OUTCOME_FINAL_FLAG_NOT_EMPTY,
                                     "$['hearingOutcome']['hearingFinalFlag']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingOutcomeHearingResult() throws Exception {
            verifyErrorOnMissingNode(HA_OUTCOME_RESULT_NOT_EMPTY,
                                     "$['hearingOutcome']['hearingResult']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenHearingResultInvalid() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_OUTCOME_RESULT_NOT_EMPTY,
                                          "$['hearingOutcome']['hearingResult']",
                                          5);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenHearingResultReasonTypeTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_OUTCOME_REASON_TYPE_MAX_LENGTH,
                                          "$['hearingOutcome']['hearingResultReasonType']",
                                          71,
                                          "HMAN80-ValidPayload2.json");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingOutcomeResultDate() throws Exception {
            verifyErrorOnMissingNode(HA_OUTCOME_REQUEST_DATE_NOT_EMPTY,
                                     "$['hearingOutcome']['hearingResultDate']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingHearingDaysHearingDate() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_HEARING_DATE_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['hearingDate']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingHearingDaysHearingStartTime() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_START_TIME_DATE_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['hearingStartTime']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingHearingDaysHearingEndTime() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_END_TIME_DATE_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['hearingEndTime']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingHearingPauseStartTime() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_PAUSE_START_TIME_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['pauseDateTimes'][0]['pauseStartTime']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingHearingPauseEndTime() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_PAUSE_END_TIME_DATE_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['pauseDateTimes'][0]['pauseEndTime']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenActualPartyIdTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_PARTY_ID_MAX_LENGTH,
                                          "$['actualHearingDays'][0]['actualDayParties'][0]['actualPartyId']",
                                          41);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingPartyRole() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_PARTY_ROLE_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['actualDayParties'][0]['partyRole']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenPartyRoleTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_PARTY_ROLE_MAX_LENGTH,
                                          "$['actualHearingDays'][0]['actualDayParties'][0]['partyRole']",
                                          41);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingPartyChannelSubType() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_PARTY_CHANNEL_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['actualDayParties'][0]['partyChannelSubType']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenPartyChannelSubTypeTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_PARTY_CHANNEL_MAX_LENGTH,
                                          "$['actualHearingDays'][0]['actualDayParties'][0]['partyChannelSubType']",
                                          71);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenRepresentedPartyTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_REPRESENTED_PARTY_MAX_LENGTH,
                                          "$['actualHearingDays'][1]['actualDayParties'][0]"
                                              + "['representedParty']",
                                          41);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingPartyIndividualFirstName() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['actualDayParties'][0]"
                                         + "['individualDetails']['firstName']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenPartyIndividualFirstNameTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_MAX_LENGTH,
                                          "$['actualHearingDays'][0]['actualDayParties'][0]"
                                              + "['individualDetails']['firstName']",
                                          101);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenMissingPartyIndividualLastName() throws Exception {
            verifyErrorOnMissingNode(HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_NOT_EMPTY,
                                     "$['actualHearingDays'][0]['actualDayParties'][0]"
                                         + "['individualDetails']['lastName']");
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenPartyIndividualLastNameTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_MAX_LENGTH,
                                          "$['actualHearingDays'][0]['actualDayParties'][0]"
                                              + "['individualDetails']['lastName']",
                                          101);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARING_ACTUALS})
        void shouldReturn400_WhenPartyOrganisationNameTooLong() throws Exception {
            verifyErrorOnTooLongNodeValue(HA_HEARING_DAY_ORGANISATION_NAME_MAX_LENGTH,
                                          "$['actualHearingDays'][1]['actualDayParties'][2]"
                                              + "['actualOrganisationName']",
                                          201);
        }

        private void verifyErrorOnMissingNode(String expectedError, String pathToDelete) throws Exception {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/HMAN80-ValidPayload4-Completed.json");
            String preparedJson = deleteByJsonPath(json, pathToDelete);

            mockMvc.perform(put(URL + "/2000001000") // LISTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(preparedJson))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem((expectedError))))
                .andReturn();
        }

        private String deleteByJsonPath(String json, String path) {
            DocumentContext jsonContext = JsonPath.parse(json);
            jsonContext.delete(path);
            return jsonContext.jsonString();
        }

        private void verifyErrorOnTooLongNodeValue(String expectedError, String pathToUpdate, int length)
            throws Exception {
            verifyErrorOnTooLongNodeValue(expectedError, pathToUpdate, length, "HMAN80-ValidPayload4-Completed.json");
        }

        private void verifyErrorOnTooLongNodeValue(String expectedError, String pathToUpdate, int length, String file)
            throws Exception {
            String json = TestFixtures.fromFileAsString("hearing-actuals-payload/" + file);
            String preparedJson = updateWithRandomStringByJsonPath(json, pathToUpdate, length);

            mockMvc.perform(put(URL + "/2000001000") // LISTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(preparedJson))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem((expectedError))))
                .andReturn();
        }

        private String updateWithRandomStringByJsonPath(String json, String path, int length) {
            DocumentContext jsonContext = JsonPath.parse(json);
            jsonContext.set(path, randomString(length));
            return jsonContext.jsonString();
        }

        private String randomString(int length) {
            return RandomStringUtils.random(length);
        }
    }
}
