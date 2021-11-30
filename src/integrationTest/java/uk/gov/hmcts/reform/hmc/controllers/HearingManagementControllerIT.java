package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn400WhileValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyValidateHearingObject;


class HearingManagementControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String url = "/hearing";
    private static final String INSERT_DATA_SCRIPT = "classpath:sql/insert-hearing.sql";

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    @Test
    @Sql(INSERT_DATA_SCRIPT)
    void shouldReturn204_WhenHearingExists() throws Exception {
        mockMvc.perform(get(url + "/123" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();

    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValid() throws Exception {
        mockMvc.perform(get(url + "/12" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    void shouldReturn204_WhenIsValidIsNotProvided() throws Exception {
        mockMvc.perform(get(url + "/12")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andReturn();
    }



    @Test
    void shouldReturn202_WhenHearingRequestIsValid() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(202))
            .andReturn();
    }

    @Test
    void shouldReturn202_WhenHearingRequestHasPartyDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(202))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingRequestHasNoRequestDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingRequestHasNoHearingDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingRequestHasNoCaseDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        stubReturn400WhileValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingRequestHasNoPanelDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
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
