package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
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
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.UpdateRequestDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200CaseDetailsByCaseId;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn400WhileValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn404FromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTO_LIST_FLAG_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_CATEGORY_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_SLA_START_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CFT_ORG_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CFT_ORG_ID_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DURATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DURATION_MIN_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FACILITIES_REQUIRED_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FIRST_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FIRST_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_EMAIL_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_EMAIL_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_PHONE_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_PHONE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_LOCATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_PRIORITY_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUESTER_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_TYPE_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INTERPRETER_LANGUAGE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_CATEGORIES;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_LOCATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PANEL_REQUIREMENTS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LAST_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LAST_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LISTING_COMMENTS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LOCATION_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NAME_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ORGANISATION_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ORGANISATION_TYPE_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PREFERRED_HEARING_CHANNEL_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PUBLIC_CASE_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PUBLIC_CASE_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATED_PARTY_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATED_PARTY_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATIONSHIP_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATIONSHIP_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REQUEST_TIMESTAMP_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.TITLE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.TITLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.UNAVAILABLE_FROM_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.UNAVAILABLE_TO_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.VERSION_NUMBER_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.VULNERABLE_DETAILS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

class HearingManagementControllerIT extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String url = "/hearing";
    public static final String USER_ID = "e8275d41-7f22-4ee7-8ed3-14644d6db096";
    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String ROLE_NAME = "Hearing Manage";
    public static final String ROLE_TYPE = "ORGANISATION";
    public static final String HEARING_NOT_FOUND_EXCEPTION = "No hearing found for reference: %s";

    private static final String INSERT_DATA_SCRIPT = "classpath:sql/insert-hearing.sql";

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

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
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestIsValid() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
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
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestHasPartyDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        createHearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
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
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoRequestDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoHearingDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoCaseDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn404_WhenHearingRequestHasNoPanelDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoRelatedPartyDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil
                                                                               .relatedPartyMandatoryFieldMissing());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenNoRoleAssignmentsFound() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        List<RoleAssignmentResource> resourceList = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(resourceList);
        stubReturn200RoleAssignments(USER_ID, roleAssignmentResponse);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors", hasItem(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, USER_ID))))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenRoleAssignmentsDoNotMeetCriteria() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
        resource.setRoleName("invalid");
        resource.setRoleType(ROLE_TYPE);
        List<RoleAssignmentResource> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(resource);
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(roleAssignmentList);
        stubReturn200RoleAssignments(USER_ID, response);
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors", hasItem(ROLE_ASSIGNMENT_INVALID_ROLE)))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenCaseCannotBeFound() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
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
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors", hasItem(CASE_NOT_FOUND)))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenRoleAssignmentDoesNotMatchCaseDetails() throws Exception {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
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
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors", hasItem(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES)))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn404_WhenDeleteHearingIdIsInValid() throws Exception {
        mockMvc.perform(delete(url + "/2000000001")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200_WhenDeleteHearingIdIsInValid() throws Exception {
        DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
        mockMvc.perform(delete(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(deleteHearingRequest)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000000"))
            .andExpect(jsonPath("$.status").value(CANCELLATION_REQUESTED))
            .andExpect(jsonPath("$.versionNumber").value(
                deleteHearingRequest.getVersionNumber() + 1))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenDeleteHearingIdIsNonNumeric() throws Exception {
        mockMvc.perform(delete(url + "/200000000P")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenDeleteHearingIdStatusInValid() throws Exception {
        mockMvc.perform(delete(url + "/2000000011")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_DELETE_HEARING_STATUS))))
            .andReturn();
    }

    @Test
    void shouldReturn200_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForValidCaseDetailsAndNoStatus() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.caseRef").value("9372710950276233"))
            .andExpect(jsonPath("$.caseHearings", hasSize(3)))
            .andExpect(jsonPath("$.caseHearings[0].hearingID").value("2000000010"))
            .andExpect(jsonPath("$.caseHearings[1].hearingID").value("2000000009"))
            .andExpect(jsonPath("$.caseHearings[2].hearingID").value("2000000000"))
            .andExpect(jsonPath("$.caseHearings[0].hmcStatus").value("HEARING_UPDATED"))
            .andExpect(jsonPath("$.caseHearings[1].hmcStatus").value("HEARING_REQUESTED"))
            .andExpect(jsonPath("$.caseHearings[2].hmcStatus").value("HEARING_REQUESTED"))
            .andExpect(jsonPath("$.hmctsServiceID").value("ABA1"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForValidCaseDetailsAndStatus() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("status", "HEARING_REQUESTED"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.caseRef").value("9372710950276233"))
            .andExpect(jsonPath("$.caseHearings", hasSize(2)))
            .andExpect(jsonPath("$.caseHearings[0].hearingID").value("2000000009"))
            .andExpect(jsonPath("$.caseHearings[1].hearingID").value("2000000000"))
            .andExpect(jsonPath("$.caseHearings[0].hmcStatus").value("HEARING_REQUESTED"))
            .andExpect(jsonPath("$.caseHearings[1].hmcStatus").value("HEARING_REQUESTED"))
            .andExpect(jsonPath("$.hmctsServiceID").value("ABA1"))
            .andReturn();
    }

    @Test
    void shouldReturn200_WhenGetHearingsForValidCaseRefLuhnAndStatus() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("status", "UPDATED"))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenGetHearingsForInvalidCaseRefNonDigits() throws Exception {
        mockMvc.perform(get("/hearings/A234567890123456")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenGetHearingsForInvalidCaseRefNonLuhn() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276230")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenGetHearingsForInvalidCaseRefSize() throws Exception {
        mockMvc.perform(get("/hearings/123456")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestIsValid() throws Exception {
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000000"))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestContainsValidPartyDetails() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdateHearingIdDoesNotMatchHearingIdInDB() throws Exception {
        mockMvc.perform(put(url + "/2000000001")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(404))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem(String.format(
                HEARING_NOT_FOUND_EXCEPTION,
                "2000000001"
            ))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenUpdateHearingIdIsNonNumeric() throws Exception {
        mockMvc.perform(put(url + "/200000000P")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenUpdateHearingIdIsInvalid() throws Exception {
        mockMvc.perform(put(url + "/3000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_HEARING_ID_DETAILS))))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenUpdateHearingIdIsTooLong() throws Exception {
        mockMvc.perform(put(url + "/200000000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_HEARING_ID_DETAILS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenVersionNumberDoesNotMatchRequest() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getRequestDetails().setVersionNumber(2);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_VERSION_NUMBER))))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenRequestIsNull() throws Exception {
        UpdateHearingRequest hearingRequest = new UpdateHearingRequest();
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(3)))
            .andExpect(jsonPath("$.errors", hasItems(INVALID_REQUEST_DETAILS, INVALID_HEARING_DETAILS,
                                                     INVALID_CASE_DETAILS
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenUpdateRequestDetailsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        UpdateRequestDetails updateRequestDetails = new UpdateRequestDetails();
        hearingRequest.setRequestDetails(updateRequestDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(2)))
            .andExpect(jsonPath("$.errors", hasItems(REQUEST_TIMESTAMP_NULL_EMPTY, VERSION_NUMBER_NULL_EMPTY)))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingDetailsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        HearingDetails hearingDetails = new HearingDetails();
        hearingRequest.setHearingDetails(hearingDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(8)))
            .andExpect(jsonPath("$.errors", hasItems(AUTO_LIST_FLAG_NULL_EMPTY, HEARING_TYPE_NULL_EMPTY,
                                                     HEARING_WINDOW_NULL, DURATION_EMPTY, HEARING_PRIORITY_TYPE,
                                                     HEARING_LOCATION_EMPTY, INVALID_HEARING_LOCATION,
                                                     INVALID_PANEL_REQUIREMENTS
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingDetailsAreInvalid() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().setHearingType("a".repeat(41));
        hearingRequest.getHearingDetails().setDuration(-1);
        List<String> nonStandardHearingDurationReasonsList = Collections.singletonList("a".repeat(71));
        hearingRequest.getHearingDetails().setNonStandardHearingDurationReasons(nonStandardHearingDurationReasonsList);
        hearingRequest.getHearingDetails().setHearingPriorityType("a".repeat(61));
        hearingRequest.getHearingDetails().setNumberOfPhysicalAttendees(-1);
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationId("invalid enum");
        List<HearingLocation> hearingLocationList = Collections.singletonList(hearingLocation);
        hearingRequest.getHearingDetails().setHearingLocations(hearingLocationList);
        List<String> facilitiesRequiredList = Collections.singletonList("a".repeat(71));
        hearingRequest.getHearingDetails().setFacilitiesRequired(facilitiesRequiredList);
        hearingRequest.getHearingDetails().setListingComments("a".repeat(5001));
        hearingRequest.getHearingDetails().setHearingRequester("a".repeat(61));
        hearingRequest.getHearingDetails().setLeadJudgeContractType("a".repeat(71));
        PanelRequirements panelRequirements = new PanelRequirements();
        List<String> listWithValueOver70Size = Collections.singletonList("a".repeat(71));
        panelRequirements.setRoleType(listWithValueOver70Size);
        panelRequirements.setAuthorisationTypes(listWithValueOver70Size);
        panelRequirements.setAuthorisationSubType(listWithValueOver70Size);
        panelRequirements.setPanelSpecialisms(listWithValueOver70Size);
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setRequirementType("MUSTINC");
        panelPreference.setMemberType("a".repeat(71));
        panelPreference.setMemberID("a".repeat(71));
        PanelPreference panelPreferenceTwo = new PanelPreference();
        List<PanelPreference> panelPreferences = Arrays.asList(panelPreference, panelPreferenceTwo);
        panelRequirements.setPanelPreferences(panelPreferences);
        hearingRequest.getHearingDetails().setPanelRequirements(panelRequirements);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(19)))
            .andExpect(jsonPath("$.errors", hasItems(HEARING_TYPE_MAX_LENGTH, DURATION_MIN_VALUE,
                                                     NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG,
                                                     HEARING_PRIORITY_TYPE_MAX_LENGTH,
                                                     NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE,
                                                     FACILITIES_REQUIRED_MAX_LENGTH_MSG, LISTING_COMMENTS_MAX_LENGTH,
                                                     HEARING_REQUESTER_MAX_LENGTH, LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH,
                                                     LOCATION_TYPE_EMPTY, "Unsupported type for locationId",
                                                     ROLE_TYPE_MAX_LENGTH_MSG, AUTHORISATION_TYPE_MAX_LENGTH_MSG,
                                                     AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG,
                                                     PANEL_SPECIALISMS_MAX_LENGTH_MSG, MEMBER_ID_EMPTY,
                                                     MEMBER_ID_MAX_LENGTH, MEMBER_TYPE_MAX_LENGTH,
                                                     "Unsupported type for requirementType"
            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenCaseDetailsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        hearingRequest.setCaseDetails(caseDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(11)))
            .andExpect(jsonPath("$.errors", hasItems(HMCTS_SERVICE_CODE_EMPTY_INVALID, CASE_REF_EMPTY,
                                                     REQUEST_TIMESTAMP_EMPTY, CASE_DEEP_LINK_EMPTY,
                                                     HMCTS_INTERNAL_CASE_NAME_EMPTY, PUBLIC_CASE_NAME_EMPTY,
                                                     CASE_CATEGORY_EMPTY, INVALID_CASE_CATEGORIES,
                                                     CASE_MANAGEMENT_LOCATION_CODE_EMPTY,
                                                     CASE_RESTRICTED_FLAG_NULL_EMPTY, CASE_SLA_START_DATE_EMPTY
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenCaseDetailsAreInvalid() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getCaseDetails().setHmctsServiceCode("invalid");
        hearingRequest.getCaseDetails().setCaseRef("invalid");
        hearingRequest.getCaseDetails().setExternalCaseReference("a".repeat(71));
        hearingRequest.getCaseDetails().setCaseDeepLink("a".repeat(1025));
        hearingRequest.getCaseDetails().setHmctsInternalCaseName("a".repeat(1025));
        hearingRequest.getCaseDetails().setPublicCaseName("a".repeat(1025));
        hearingRequest.getCaseDetails().setCaseManagementLocationCode("a".repeat(41));
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("a".repeat(71));
        List<CaseCategory> caseCategoryList = Collections.singletonList(category);
        hearingRequest.getCaseDetails().setCaseCategories(caseCategoryList);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(10)))
            .andExpect(jsonPath("$.errors", hasItems(HMCTS_SERVICE_CODE_EMPTY_INVALID, CASE_REF_INVALID,
                                                     EXTERNAL_CASE_REFERENCE_MAX_LENGTH, CASE_DEEP_LINK_MAX_LENGTH,
                                                     CASE_DEEP_LINK_INVALID, HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH,
                                                     PUBLIC_CASE_NAME_MAX_LENGTH,
                                                     CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH, CATEGORY_VALUE,
                                                     "Unsupported type for categoryType"
            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenPartyDetailsNull() throws Exception {
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        RelatedParty relatedParty = new RelatedParty();
        individualDetails.setRelatedParties(Collections.singletonList(relatedParty));
        OrganisationDetails organisationDetails = new OrganisationDetails();
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        List<UnavailabilityDow> unavailabilityDowList = Collections.singletonList(unavailabilityDow);
        UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
        List<UnavailabilityRanges> unavailabilityRangesList = Collections.singletonList(unavailabilityRanges);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(unavailabilityRangesList);
        partyDetails.setUnavailabilityDow(unavailabilityDowList);
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.setPartyDetails(Collections.singletonList(partyDetails));
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(14)))
            .andExpect(jsonPath("$.errors", hasItems(PARTY_DETAILS_NULL_EMPTY,
                                                     "Unsupported type for partyType", UNAVAILABLE_FROM_DATE_EMPTY,
                                                     UNAVAILABLE_TO_DATE_EMPTY, "Unsupported type for dow",
                                                     "Unsupported type for dowUnavailabilityType", NAME_NULL_EMPTY,
                                                     ORGANISATION_TYPE_NULL_EMPTY, CFT_ORG_ID_NULL_EMPTY, TITLE_EMPTY,
                                                     FIRST_NAME_EMPTY, LAST_NAME_EMPTY, RELATED_PARTY_EMPTY,
                                                     RELATIONSHIP_TYPE_EMPTY
            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenPartyDetailsInvalid() throws Exception {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("a".repeat(41));
        partyDetails.setPartyRole("a".repeat(7));
        partyDetails.setPartyType("IND");
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("a".repeat(41));
        individualDetails.setFirstName("a".repeat(101));
        individualDetails.setLastName("a".repeat(101));
        individualDetails.setPreferredHearingChannel("a".repeat(71));
        individualDetails.setInterpreterLanguage("a".repeat(11));
        individualDetails.setReasonableAdjustments(Collections.singletonList("a".repeat(11)));
        individualDetails.setVulnerabilityDetails("a".repeat(257));
        individualDetails.setHearingChannelEmail("a".repeat(121));
        individualDetails.setHearingChannelPhone("a".repeat(31));
        RelatedParty relatedParty = new RelatedParty();
        relatedParty.setRelatedPartyID("a".repeat(2001));
        relatedParty.setRelationshipType("a".repeat(2001));
        individualDetails.setRelatedParties(Collections.singletonList(relatedParty));
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("a".repeat(2001));
        organisationDetails.setOrganisationType("a".repeat(61));
        organisationDetails.setCftOrganisationID("a".repeat(61));
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.setPartyDetails(Collections.singletonList(partyDetails));
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(18)))
            .andExpect(jsonPath("$.errors", hasItems(PARTY_DETAILS_MAX_LENGTH, PARTY_ROLE_MAX_LENGTH,
                                                     TITLE_MAX_LENGTH, FIRST_NAME_MAX_LENGTH, LAST_NAME_MAX_LENGTH,
                                                     PREFERRED_HEARING_CHANNEL_MAX_LENGTH,
                                                     INTERPRETER_LANGUAGE_MAX_LENGTH,
                                                     REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG,
                                                     VULNERABLE_DETAILS_MAX_LENGTH, HEARING_CHANNEL_EMAIL_MAX_LENGTH,
                                                     HEARING_CHANNEL_PHONE_MAX_LENGTH, HEARING_CHANNEL_PHONE_INVALID,
                                                     RELATED_PARTY_MAX_LENGTH, RELATIONSHIP_TYPE_MAX_LENGTH,
                                                     NAME_MAX_LENGTH, ORGANISATION_TYPE_MAX_LENGTH,
                                                     CFT_ORG_ID_MAX_LENGTH, HEARING_CHANNEL_EMAIL_INVALID
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingWindowFieldsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().setHearingWindow(null);
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_WINDOW_NULL))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenPartyIndividualAndOrgDetailsNull() throws Exception {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        request.setPartyDetails(TestingUtil.partyDetails());
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_ORG_INDIVIDUAL_DETAILS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenPartyIndividualAndOrgDetailsBothExist() throws Exception {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        request.setPartyDetails(TestingUtil.partyDetails());
        request.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        request.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        mockMvc.perform(put(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_ORG_INDIVIDUAL_DETAILS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenUpdateHearingStatusIsInvalid() throws Exception {
        mockMvc.perform(put(url + "/2000000011")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_PUT_HEARING_STATUS))))
            .andReturn();
    }
}
