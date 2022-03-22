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
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HearingActualsManagementControllerIT extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/hearingActuals";

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Nested
    @DisplayName("PutHearingActuals")
    class PutHearingActuals {
        @Test
        void shouldReturn404_WhenHearingIdDoesNotExist() throws Exception {
            mockMvc.perform(put(url + "/2990000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(TestingUtil.hearingActual())))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem("001 No such id: 2990000001")))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsInvalid() throws Exception {
            mockMvc.perform(put(url + "/1000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(TestingUtil.hearingActual())))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem("Invalid hearing Id")))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
        void shouldReturn400_WhenHearingIdStatusInValid() throws Exception {
            mockMvc.perform(put(url + "/2000000000") // status HEARING_REQUESTED
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(TestingUtil.hearingActual())))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(("002 invalid status HEARING_REQUESTED"))))
                .andReturn();
        }


    }
}
