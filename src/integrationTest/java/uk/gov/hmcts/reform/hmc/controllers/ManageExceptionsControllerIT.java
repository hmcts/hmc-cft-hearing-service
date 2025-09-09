package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_SUPPORT_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MANAGE_EXCEPTION_ACTION_EMPTY;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertJsonToRequest;

class ManageExceptionsControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String url = "/manageExceptions";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_HEARINGS = "classpath:sql/get-hearings-ManageSupportRequest.sql";


    ManageExceptionRequest finalStateRequest = convertJsonToRequest(
        "manage-exceptions/valid-final_state_transition_request.json");
    ManageExceptionRequest rollBackRequest = convertJsonToRequest(
        "manage-exceptions/valid-roll_back_request.json");

    private static final String SUCCESS_STATUS = ManageRequestStatus.SUCCESSFUL.label;

    ManageExceptionsControllerIT() throws IOException {
    }

    @Test
    void shouldReturn400_WhenManageExceptionRequestIsEmpty() throws Exception {
        MvcResult response =  mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(new ManageExceptionRequest())))
            .andExpect(status().is(400))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        assertThat(responseBody.contains(BAD_REQUEST.toString()));
        assertThat(responseBody.contains(INVALID_SUPPORT_REQUEST_DETAILS));
    }

    @Test
    void shouldReturn400_WhenHearingIdIsEmpty() throws Exception {
        ManageExceptionRequest request = TestingUtil.invalidManageExceptionRequest();
        MvcResult response =  mockMvc.perform(post(url)
                                                  .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                  .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(400))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        assertThat(responseBody.contains(BAD_REQUEST.toString()));
        assertThat(responseBody.contains(HEARING_ID_EMPTY));
    }

    @Test
    void shouldReturn400_WhenActionAndStareIsEmpty() throws Exception {
        ManageExceptionRequest request = TestingUtil.manageExceptionRequest_StateAndActionEmpty();
        MvcResult response =  mockMvc.perform(post(url)
                                                  .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                  .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(400))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        assertThat(responseBody.contains(BAD_REQUEST.toString()));
        assertThat(responseBody.contains(MANAGE_EXCEPTION_ACTION_EMPTY));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
    void shouldReturn200_ValidRollBackRequest() throws Exception {
        MvcResult response =  mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(rollBackRequest)))
            .andExpect(status().is(200))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        assertThat(responseBody.contains("2000000000"));
        assertThat(responseBody.contains("successfully transitioned hearing : 2000000000, from state : EXCEPTION "
                                             + "to state: CANCELLATION_SUBMITTED"));
        assertThat(responseBody.contains("2000000001"));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
    void shouldReturn200_ValidFinalStateRequest() throws Exception {
        MvcResult response =  mockMvc.perform(post(url)
                                                  .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                  .content(objectMapper.writeValueAsString(finalStateRequest)))
            .andExpect(status().is(200))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        ManageExceptionResponse manageExceptionResponse =
            objectMapper.readValue(responseBody, ManageExceptionResponse.class);

        assertThat(manageExceptionResponse.getSupportRequestResponse()).hasSize(3);
        assertThat(manageExceptionResponse.getSupportRequestResponse().get(2).getHearingId()).isEqualTo("2000000002");
        assertThat(manageExceptionResponse.getSupportRequestResponse().get(2).getStatus()).isEqualTo(SUCCESS_STATUS);
        assertThat(manageExceptionResponse.getSupportRequestResponse().get(2).getMessage()).isEqualTo("successfully "
                                +  "transitioned hearing : 2000000002, from state : EXCEPTION to state: ADJOURNED");
    }

    private final String serviceJwtXuiWeb = generateDummyS2SToken("tech_admin_ui");
}
