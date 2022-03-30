package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.config.MessageReaderFromQueueConfiguration;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class HearingActualControllerIT extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(HearingActualControllerIT.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationParams applicationParams;

    private static final String url = "/hearingActuals";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-HearingsActual_request.sql";

    @Nested
    @DisplayName("Get Hearing Actuals")
    class GetHearingActuals {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenHearingExists() throws Exception {
            mockMvc.perform(get(url + "/2000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        void shouldReturn404_WhenHearingDoesNotExist() throws Exception {
            mockMvc.perform(get(url + "/2000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem("No hearing found for reference: 2000000001")))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsMalformed() throws Exception {
            mockMvc.perform(get(url + "/1000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.INVALID_HEARING_ID_DETAILS)))
                .andReturn();
        }
    }
}
