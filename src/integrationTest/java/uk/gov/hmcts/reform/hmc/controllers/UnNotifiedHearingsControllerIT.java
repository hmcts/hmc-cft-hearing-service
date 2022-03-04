package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnNotifiedHearingsControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/unNotifiedHearings";
    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200_WhenDetailsAreValid() throws Exception {
        mockMvc.perform(get(url + "/ABA1?hearing_start_date_from=2020-02-20 11:20:00"
                                + "&hearing_start_date_to=2020-02-20 11:20:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenHmctsServiceIdIsInValid() throws Exception {
        mockMvc.perform(get(url + "/1111?hearing_start_date_from=2020-02-20 11:20:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenHearingStartDateFromIsInValid() throws Exception {
        mockMvc.perform(get(url + "/ABA1?hearing_start_date_from=aa")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200_WhenHearingStartDateToIsInValid() throws Exception {
        mockMvc.perform(get(url + "/ABA1?hearing_start_date_from=2020-02-20 11:20:00"
                                + "&hearing_start_date_to=aa")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenHearingStartDateFromIsNotPresent() throws Exception {
        mockMvc.perform(get(url + "/ABA1?hearing_start_date_to=2020-02-20 11:20:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }
}
