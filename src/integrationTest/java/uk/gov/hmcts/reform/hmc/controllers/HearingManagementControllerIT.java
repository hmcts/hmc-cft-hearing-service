package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class HearingManagementControllerIT {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String getHearingUrl = "/hearing";

    @Test
    void shouldReturn204_WhenHearingExists() throws Exception {
       // stubSuccessfullyValidateHearingObject(hearingRequest);
        mockMvc.perform(get(getHearingUrl+ "/10")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValid() throws Exception {

        mockMvc.perform(get(getHearingUrl+"/12")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }
}
