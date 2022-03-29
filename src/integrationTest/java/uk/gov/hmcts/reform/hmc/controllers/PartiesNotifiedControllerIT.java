package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND;

class PartiesNotifiedControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationParams applicationParams;

    private static final String url = "/partiesNotified";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenPartiesNotifiedIsSuccess() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            mockMvc.perform(put(url + "/2000000000" + "?version=2")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsInValid() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            mockMvc.perform(put(url + "/1000000000" + "?version=2")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(INVALID_HEARING_ID_DETAILS)))
                .andReturn();
        }

        @Test
        void shouldReturn404_WhenHearingIdDoesNotExist() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            mockMvc.perform(put(url + "/2000000001" + "?version=2")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem("001 No such id: 2000000001")))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn404_WhenResponseVersionDoesNotMatch() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            mockMvc.perform(put(url + "/2000000000" + "?version=25")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem("002 No such response version")))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenPartiesNotifiedIsAlreadySet() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            mockMvc.perform(put(url + "/2000000010" + "?version=2")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("003 Already set")))
                .andReturn();
        }
    }

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
                .andExpect(jsonPath("$.errors", hasItem(PARTIES_NOTIFIED_ID_NOT_FOUND.replace("%s", "2000000001"))))
                .andReturn();
        }

    }

}
