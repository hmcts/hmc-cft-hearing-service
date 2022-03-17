package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class LinkedHearingGroupControllerIT extends BaseTest {
    public static String ERROR_PATH_ERROR = "$.errors";

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/linkedHearingGroup";
    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";

    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn404_WhenHearingGroupDoesNotExist() throws Exception {
        mockMvc.perform(delete(url + "/7600000123")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingGroupStatusIsPending() throws Exception {
        mockMvc.perform(delete(url + "/7600000501")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value("007 group is in a PENDING state"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingGroupStatusIsError() throws Exception {
        mockMvc.perform(delete(url + "/7600000502")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value("007 group is in a ERROR state"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingGroupHearingResponseStartDateIsInThePastForHearingStatusHEARING_REQUESTED()
        throws Exception {
        mockMvc.perform(delete(url + "/7600000300")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath(ERROR_PATH_ERROR)
                           .value("004 Invalid start date in the past for unlinking hearing request 2000000301"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingGroupHearingResponseStartDateIsInThePastForHearingStatusUPDATE_REQUESTED()
        throws Exception {
        mockMvc.perform(delete(url + "/7600000301")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath(ERROR_PATH_ERROR)
                           .value("004 Invalid start date in the past for unlinking hearing request 2000000302"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenHearingGroupExists() throws Exception {
        mockMvc.perform(delete(url + "/7600000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

}
