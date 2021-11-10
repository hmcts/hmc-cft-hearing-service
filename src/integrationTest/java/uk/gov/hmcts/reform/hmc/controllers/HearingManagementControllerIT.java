package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn404InValidHearingId;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyForValidHearingID;


class HearingManagementControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String getHearingUrl = "/hearing";

    private static final String INSERT_DATA_SCRIPT = "classpath:sql/insert-hearing.sql";


    @Test
    @Sql(INSERT_DATA_SCRIPT)
    void shouldReturn204_WhenHearingExists() throws Exception {
        stubSuccessfullyForValidHearingID("123");
        mockMvc.perform(get(getHearingUrl+ "/123")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValid() throws Exception {
        stubReturn404InValidHearingId("12");
        mockMvc.perform(get(getHearingUrl+"/12")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }
}
