package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.JURISDICTION;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.ROLE_TYPE;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.USER_ID;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NON_UNIQUE_HEARING_RESPONSE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ALREADY_SET;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_NO_SUCH_RESPONSE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;

class PartiesNotifiedControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/partiesNotified";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private void stubRoleAssignments() {
        RoleAssignmentResource resource = new RoleAssignmentResource();
        resource.setRoleName(HEARING_MANAGER);
        resource.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource attributesResource = new RoleAssignmentAttributesResource();
        attributesResource.setCaseType(Optional.of(CASE_TYPE));
        attributesResource.setJurisdiction(Optional.of(JURISDICTION));
        resource.setAttributes(attributesResource);

        RoleAssignmentResource hearingViewer = new RoleAssignmentResource();
        hearingViewer.setRoleName(HEARING_VIEWER);
        hearingViewer.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource hearingViewerResource = new RoleAssignmentAttributesResource();
        hearingViewerResource.setCaseType(Optional.of(CASE_TYPE));
        hearingViewerResource.setJurisdiction(Optional.of(JURISDICTION));
        hearingViewer.setAttributes(hearingViewerResource);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        roleAssignmentList.add(hearingViewer);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
    }

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {

        @BeforeEach
        void setUp() {
            stubRoleAssignments();
        }

        private final String serviceJwtXuiWeb = generateDummyS2SToken("ccd_definition");

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenPartiesNotifiedIsSuccess() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);
            final String dateTime = "2020-08-10T11:20:00";
            mockMvc.perform(put(url + "/2000000000" + "?version=1&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(200))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenHearingResponse_IsNotUnique() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            final String dateTime = "2020-08-10T14:20:00";
            mockMvc.perform(put(url + "/2000000013" + "?version=1&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(NON_UNIQUE_HEARING_RESPONSE)))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsInValid() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            final String dateTime = "2020-11-30T10:15:21";
            mockMvc.perform(put(url + "/1000000000" + "?version=2&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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

            final String dateTime = "2020-11-30T10:15:21";
            mockMvc.perform(put(url + "/2000000001" + "?version=2&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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

            final String dateTime = "2020-11-30T10:15:21";
            mockMvc.perform(put(url + "/2000000000" + "?version=25&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.errors", hasItem(PARTIES_NOTIFIED_NO_SUCH_RESPONSE)))
                .andReturn();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn400_WhenPartiesNotifiedIsAlreadySet() throws Exception {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            final String dateTime = "2021-08-10T11:20:00";
            mockMvc.perform(put(url + "/2000000010" + "?version=1&received=" + dateTime)
                                .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(partiesNotified)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(PARTIES_NOTIFIED_ALREADY_SET)))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("GetPartiesNotified")
    class GetPartiesNotified {

        @BeforeEach
        void setUp() {
            stubRoleAssignments();
        }

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
