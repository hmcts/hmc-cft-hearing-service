package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
        mockMvc.perform(get(getHearingUrl + "/123" + "?isValid")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();

    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValid() throws Exception {
        MvcResult result = mockMvc.perform(get(getHearingUrl + "/12" + "?isValid")
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();

        assertEquals("No hearing found for reference: 12", result.getResolvedException().getMessage());
    }
}
