package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200CaseDetailsByCaseId;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn400WhileValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn404FromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

class HearingManagementControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String url = "/hearing";
    public static final String USER_ID = "e8275d41-7f22-4ee7-8ed3-14644d6db096";
    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String ROLE_NAME = "Hearing Manage";
    public static final String ROLE_TYPE = "ORGANISATION";

    private static final String INSERT_DATA_SCRIPT = "classpath:sql/insert-hearing.sql";

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
    void shouldReturn201_WhenHearingRequestIsValid() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        RoleAssignmentResource resource =  new RoleAssignmentResource();
        resource.setRoleName(ROLE_NAME);
        resource.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource attributesResource = new RoleAssignmentAttributesResource();
        attributesResource.setCaseType(Optional.of(CASE_TYPE));
        attributesResource.setJurisdiction(Optional.of(JURISDICTION));
        resource.setAttributes(attributesResource);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    void shouldReturn201_WhenHearingRequestHasPartyDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        RoleAssignmentResource resource =  new RoleAssignmentResource();
        resource.setRoleName(ROLE_NAME);
        resource.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource attributesResource = new RoleAssignmentAttributesResource();
        attributesResource.setCaseType(Optional.of(CASE_TYPE));
        attributesResource.setJurisdiction(Optional.of(JURISDICTION));
        resource.setAttributes(attributesResource);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
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
    void shouldReturn403WhenNoRoleAssignmentsFound() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        List<RoleAssignmentResource> resourceList =  new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(resourceList);
        stubReturn200RoleAssignments(USER_ID, roleAssignmentResponse);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors",hasItem(String.format(ROLE_ASSIGNMENTS_NOT_FOUND,  USER_ID))))
            .andReturn();
    }

    @Test
    void shouldReturn403WhenRoleAssignmentsDoNotMeetCriteria() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        RoleAssignmentResource resource =  new RoleAssignmentResource();
        resource.setRoleName("invalid");
        resource.setRoleType(ROLE_TYPE);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors",hasItem(ROLE_ASSIGNMENT_INVALID_ROLE)))
            .andReturn();
    }

    @Test
    void shouldReturn403WhenCaseCannotBeFound() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        RoleAssignmentResource resource =  new RoleAssignmentResource();
        resource.setRoleName(ROLE_NAME);
        resource.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource attributesResource = new RoleAssignmentAttributesResource();
        attributesResource.setCaseType(Optional.of(CASE_TYPE));
        attributesResource.setJurisdiction(Optional.of(JURISDICTION));
        resource.setAttributes(attributesResource);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        stubReturn404FromDataStore(CASE_REFERENCE);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors",hasItem(CASE_NOT_FOUND)))
            .andReturn();
    }

    @Test
    void shouldReturn403WhenRoleAssignmentDoesNotMatchCaseDetails() throws Exception {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(hearingRequest);
        RoleAssignmentResource resource =  new RoleAssignmentResource();
        resource.setRoleName(ROLE_NAME);
        resource.setRoleType(ROLE_TYPE);
        RoleAssignmentAttributesResource attributesResource = new RoleAssignmentAttributesResource();
        attributesResource.setCaseType(Optional.of(CASE_TYPE));
        attributesResource.setJurisdiction(Optional.of(JURISDICTION));
        resource.setAttributes(attributesResource);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId("invalidCaseType")
            .jurisdiction("invalidJurisdiction")
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors",hasItem(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES)))
            .andReturn();
    }
}
