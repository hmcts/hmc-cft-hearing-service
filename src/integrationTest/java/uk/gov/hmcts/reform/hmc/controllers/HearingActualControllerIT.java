package uk.gov.hmcts.reform.hmc.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.getJsonString;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.JURISDICTION;
import static uk.gov.hmcts.reform.hmc.controllers.HearingManagementControllerIT.USER_ID;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;


class HearingActualControllerIT extends BaseTest {

    public static final String ROLE_TYPE = "ORGANISATION";

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/hearingActuals";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-HearingsActual_request.sql";

    @Nested
    @DisplayName("Get Hearing Actuals")
    class GetHearingActuals {

        @BeforeEach
        void setUp() {
            RoleAssignmentResponse response = stubRoleAssignments();
            stubReturn200RoleAssignments(USER_ID, response);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenHearingExists() throws Exception {
            mockMvc.perform(get(URL + "/2000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();
        }

        @Test
        void shouldReturn404_WhenHearingDoesNotExist() throws Exception {
            mockMvc.perform(get(URL + "/2000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errors", hasItem("No hearing found for reference: 2000000001")))
                .andReturn();
        }

        @Test
        void shouldReturn400_WhenHearingIdIsMalformed() throws Exception {
            mockMvc.perform(get(URL + "/1000000000")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.INVALID_HEARING_ID_DETAILS)))
                .andReturn();
        }
    }

    @Nested
    @DisplayName("Get Hearing Actuals with alternative data store and role assignment endpoints")
    class GetHearingActualsAlternativeEndpoints {

        static WireMockServer amServer;
        static WireMockServer dataStoreServer;

        @BeforeAll
        static void startServers() {
            int amPort = 23456;
            int dataStorePort = 34567;
            amServer = startExtraWireMock(amPort, "/am/role-assignments.*", getJsonString(stubRoleAssignments()));
            dataStoreServer = startExtraWireMock(dataStorePort, "/cases/.*", CCD_RESPONSE);
            amServer.start();
            dataStoreServer.start();
        }

        @AfterAll
        static void stopServers() {
            amServer.stop();
            dataStoreServer.stop();
        }

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
            amServer.resetRequests();
            dataStoreServer.resetRequests();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldCallProvidedCcdAndAmUrl_WhenHeadersProvided() throws Exception {
            mockMvc.perform(get(URL + "/2000000000")
                                .header(dataStoreUrlManager.getUrlHeaderName(), dataStoreServer.baseUrl())
                                .header(roleAssignmentUrlManager.getUrlHeaderName(), amServer.baseUrl())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

            amServer.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/am/role-assignments/actors/" + USER_ID)));
            dataStoreServer.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/cases/9372710950276233")));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldCallProvidedCcdUrl_WhenCcdHeaderProvided() throws Exception {
            mockMvc.perform(get(URL + "/2000000000")
                                .header(dataStoreUrlManager.getUrlHeaderName(), dataStoreServer.baseUrl())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
            dataStoreServer.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/cases/9372710950276233")));
            WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/am/role-assignments/actors/" + USER_ID)));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldCallProvidedAmUrl_WhenAmHeaderProvided() throws Exception {
            mockMvc.perform(get(URL + "/2000000000")
                                .header(roleAssignmentUrlManager.getUrlHeaderName(), amServer.baseUrl())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

            amServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/am/role-assignments/actors/" + USER_ID)));
            WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/cases/9372710950276233")));
        }
    }

    private static RoleAssignmentResponse stubRoleAssignments() {
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
        return response;
    }


}
