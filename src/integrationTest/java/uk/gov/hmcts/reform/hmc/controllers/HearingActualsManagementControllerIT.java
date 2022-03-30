package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.actualHearingDay;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingActualsOutcome;

class HearingActualsManagementControllerIT extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/hearingActuals";
    private static final String INSERT_HEARINGS_RESPONSE_SCRIPT = "classpath:sql/insert-hearings-with-response.sql";
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfListed()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001000") // LISTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload1.json")))
                .andExpect(status().is(200))
                .andReturn();
            // TODO: call GET /hearingActuals and assert on the response
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-09
        // https://tools.hmcts.net/jira/browse/HMAN-82 AC02
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfUpdateRequested()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001100") // UPDATE_REQUESTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload2.json")))
                .andExpect(status().is(200))
                .andReturn();
            // TODO: call GET /hearingActuals and assert on the response
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-10
        // https://tools.hmcts.net/jira/browse/HMAN-82 AC03
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
        void shouldReturn200_WhenSuppliedValidPayloadForHearingStatusOfUpdateSubmitted()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload3-no-partyId-supplied.json")))
                .andExpect(status().is(200))
                .andReturn();
            // TODO: call GET /hearingActuals and assert on the response
        }

        // https://tools.hmcts.net/jira/browse/HMAN-82 AC04
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
        void shouldReturn200_WhenSuppliedValidPayloadWithHearingResultAsCompleted()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload4-Completed.json")))
                .andExpect(status().is(200))
                .andReturn();
            // TODO: call GET /hearingActuals and assert on the response
        }

        // https://tools.hmcts.net/jira/browse/HMAN-82 AC05
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
        void shouldReturn200_WhenSuppliedValidPayloadWithHearingResultAsCancelled()
            throws Exception {
            mockMvc.perform(put(URL + "/2000001200") // UPDATE_SUBMITTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(TestFixtures.fromFileAsString(
                                    "hearing-actuals-payload/HMAN80-ValidPayload5-Cancelled.json")))
                .andExpect(status().is(200))
                .andReturn();
            // TODO: call GET /hearingActuals and assert on the response
        }

        // https://tools.hmcts.net/jira/browse/HHMAN-80 AC-11
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS_RESPONSE_SCRIPT})
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
}
