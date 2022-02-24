package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.HEARING_NOT_FOUND_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

class PartiesNotifiedControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/partiesNotified";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    @Nested
    @DisplayName("GetPartiesNotified")
    class GetPartiesNotified {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenPartiesNotifiedIsSuccess() throws Exception {
            mockMvc.perform(get(url + "/2000000000"))
                    .andExpect(status().is(200))
                    .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsInValid() throws Exception {
            mockMvc.perform(get(url + "/1000000000"))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.errors", hasItem(INVALID_HEARING_ID_DETAILS)))
                    .andReturn();
        }

        @Test
        void shouldReturn404_WhenHearingIdDoesNotExist() throws Exception {
            mockMvc.perform(get(url + "/2000000001"))
                    .andExpect(status().is(404))
                    .andExpect(jsonPath("$.errors", hasItem(HEARING_NOT_FOUND_EXCEPTION.replace("%s", "2000000001"))))
                    .andReturn();
        }

    }

}