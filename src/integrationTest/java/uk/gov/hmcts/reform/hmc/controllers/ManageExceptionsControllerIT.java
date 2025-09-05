package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertJsonToRequest;

class ManageExceptionsControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String url = "/manageExceptions";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_HEARINGS = "classpath:sql/get-hearings-ManageSupportRequest.sql";

    private static final String ID = "4d96923f-891a-4cb1-863e-9bec44d1689d";


    //ManageExceptionRequest finalStateRequest = convertJsonToRequest("manage-exceptions/valid-final_state_transition_request.json");
    //ManageExceptionRequest rollBackRequest = convertJsonToRequest("manage-exceptions/valid-roll_back_request.json");

    @Test
    void shouldReturn400_WhenManageExceptionRequestIsEmpty() throws Exception {
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(new ManageExceptionRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_HEARINGS})
    void shouldReturn200_WhenManageExceptionRequestIsEmpty() throws Exception {
        ManageExceptionRequest rollBackRequest = convertJsonToRequest(
            "manage-exceptions/valid-roll_back_request.json");
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(rollBackRequest)))
            .andExpect(status().is(200))
            .andReturn();
    }

    private final String serviceJwtXuiWeb = generateDummyS2SToken("tech_admin_ui");
}
