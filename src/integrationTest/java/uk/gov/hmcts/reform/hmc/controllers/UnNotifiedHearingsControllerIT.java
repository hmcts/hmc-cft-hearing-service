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
    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenDetailsWithOutStartDateTo() throws Exception {
        mockMvc.perform(get(url + "/ACA2?hearing_start_date_from=2019-01-01 11:00:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenDetailsWithStartDateTo() throws Exception {
        mockMvc.perform(get(url + "/ACA2?hearing_start_date_from=2019-01-01 11:00:00"
                                + "&hearing_start_date_to=2022-01-01 11:00:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHmctsServiceIdIsNotInDB() throws Exception {
        mockMvc.perform(get(url + "/1111?hearing_start_date_from=2020-02-20 11:20:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingStartDateFromIsInValid() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_from=aa")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenHearingStartDateToIsInValid() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_from=2020-02-20 11:20:00"
                                + "&hearing_start_date_to=aa")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenHearingStartDateFromIsNotPresent() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_to=2020-02-20 11:20:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenStartDateToFormatIsInvalid() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_from=2019-01-01 11:00:00"
                                + "&hearing_start_date_to=2022-01-01")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenStartDateFromFormatIsInvalid() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_from=2019-01-01"
                                + "&hearing_start_date_to=2022-01-01 11:00:00")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void shouldReturn400_WhenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get(url + "/TEST?hearing_start_date_from=2019-01-01"
                                + "&hearing_start_date_to=2022-01-01")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }
}
