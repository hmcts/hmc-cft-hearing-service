package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class HearingManagementControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String getHearingUrl = "/hearing";

    private static final String INSERT_DATA_SCRIPT = "classpath:sql/insert-hearing.sql";

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    @Test
    @Sql(INSERT_DATA_SCRIPT)
    void shouldReturn204_WhenHearingExists() throws Exception {
        mockMvc.perform(get(getHearingUrl + "/123" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();

    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValid() throws Exception {
        mockMvc.perform(get(getHearingUrl + "/12" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    void shouldReturn204_WhenIsValidIsNotProvided() throws Exception {
        mockMvc.perform(get(getHearingUrl + "/12")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void shouldReturn404_WhenDeleteHearingIdIsInValid() throws Exception {
        mockMvc.perform(delete(getHearingUrl + "/2000000001")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void shouldReturn200_WhenDeleteHearingIdIsInValid() throws Exception {
        mockMvc.perform(delete(getHearingUrl + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenDeleteHearingIdIsNonNumeric() throws Exception {
        mockMvc.perform(delete(getHearingUrl + "/200000000P")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

}
