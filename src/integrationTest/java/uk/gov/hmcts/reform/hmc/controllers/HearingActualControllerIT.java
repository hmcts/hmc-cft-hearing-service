package uk.gov.hmcts.reform.hmc.controllers;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
            stubRoleAssignments();
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
        void shouldReturn200_WhenHearingExists() throws Exception {
            stubFor(WireMock.get(urlMatching("/cases/9372710950276233"))
                        .willReturn(okJson("""
                              {
                                "jurisdiction": "Jurisdiction1",
                                "case_type": "CaseType1"
                              }
                              """)));
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
    }
}
