package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.val;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.ChangeReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.repository.CancellationReasonsRepository;
import uk.gov.hmcts.reform.hmc.repository.ChangeReasonsRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import wiremock.com.jayway.jsonpath.JsonPath;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200CaseDetailsByCaseId;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200ForAllCasesFromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200ForAllCasesFromDataStorePaginated;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn400WhileValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn404FromDataStore;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyValidateHearingObject;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AMEND_REASON_CODE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTO_LIST_FLAG_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CANCELLATION_REASON_CODE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_CATEGORY_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_DEEP_LINK_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_SLA_START_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CFT_ORG_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CUSTODY_STATUS_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DURATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DURATION_MIN_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FACILITIES_REQUIRED_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FIRST_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FIRST_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_EMAIL_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_EMAIL_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_PHONE_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CHANNEL_PHONE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_LOCATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_PRIORITY_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUESTER_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_TYPE_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_DETAILS_ARE_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_EMPTY_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_DEPLOYMENT_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_DEPLOYMENT_ID_NOT_REQUIRED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INTERPRETER_LANGUAGE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_AMEND_REASON_CODE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CANCELLATION_REASON_CODE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_CATEGORIES;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_REFERENCE;
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
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LISTING_REASON_CODE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LOCATION_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MEMBER_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NAME_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ORGANISATION_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ORGANISATION_TYPE_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.OTHER_REASON_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PREFERRED_HEARING_CHANNEL_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PUBLIC_CASE_NAME_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PUBLIC_CASE_NAME_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATED_PARTY_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATED_PARTY_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATIONSHIP_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.RELATIONSHIP_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.TITLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.UNAVAILABLE_FROM_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.UNAVAILABLE_TO_DATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.VERSION_NUMBER_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.VULNERABLE_DETAILS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CANCELLATION_REASON_CODES;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

class HearingManagementControllerIT extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(HearingManagementControllerIT.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CancellationReasonsRepository cancellationReasonsRepository;

    @Autowired
    private ChangeReasonsRepository changeReasonsRepository;

    private static final String url = "/hearing";
    public static final String USER_ID = "e8275d41-7f22-4ee7-8ed3-14644d6db096";
    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String ROLE_NAME = "Hearing Manage";
    public static final String ROLE_TYPE = "ORGANISATION";
    public static final String HEARING_NOT_FOUND_EXCEPTION = "No hearing found for reference: %s";
    private static final String hearingCompletion = "/hearingActualsCompletion";

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";
    private static final String CASE_HEARING_ACTUAL_HEARING = "classpath:sql/insert-caseHearings_actualhearings.sql";
    private static final String UPDATE_HEARINGS_DATA_SCRIPT = "classpath:sql/update-case-hearing-request.sql";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    private static final String INSERT_CASE_HEARING_REQUEST_PAGINATED_DATA_SCRIPT =
        "classpath:sql/insert-case_hearing_request_paginated.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn204_WhenHearingExists() throws Exception {
        stubRoleAssignments();
        mockMvc.perform(get(url + "/2000000136" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(204))
            .andExpect(header().exists("Latest-Hearing-Request-Version"))
            .andExpect(header().string("Latest-Hearing-Request-Version", "3"))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenHearingIdIsInValid() throws Exception {
        mockMvc.perform(get(url + "/1000000000" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingIdDoesNotExist() throws Exception {
        mockMvc.perform(get(url + "/2000000001" + "?isValid=true")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenHearingExistsInDb() throws Exception {
        mockMvc.perform(get(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenHearingHasCancellationReasons() throws Exception {
        mockMvc.perform(get(url + "/2000000012")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.requestDetails.cancellationReasonCodes").value(IsNull.notNullValue()))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenHearingStatusIsListed() throws Exception {
        mockMvc.perform(get(url + "/2000000011")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }


    @Test
    void shouldReturn404_WhenHearingIdIsInValidInDbAndParamIsFalse() throws Exception {
        mockMvc.perform(get(url + "/2000000010" + "?isValid=false")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    void shouldReturn404_WhenHearingIdIsInValidInDb() throws Exception {
        mockMvc.perform(get(url + "/2000000010")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andReturn();
    }


    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenIsValidIsNotProvided() throws Exception {
        mockMvc.perform(get(url + "/2000000000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestIsValid() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.getValidCaseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestHasPartyDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.getValidCaseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        UnavailabilityDow unavailabilityDowMonday = new UnavailabilityDow();
        unavailabilityDowMonday.setDow("Monday");
        unavailabilityDowMonday.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.label);
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestHasPartyDetailsWhereOrgIdNull() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.getValidCaseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        RoleAssignmentResource resource = new RoleAssignmentResource();
        resource.setRoleName(ROLE_NAME);
        resource.setRoleType(ROLE_TYPE);
        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenUpdateHearingRequestHasNoRequestDetails() throws Exception {
        UpdateHearingRequest updateHearingRequest = new UpdateHearingRequest();
        updateHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        updateHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        updateHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(updateHearingRequest);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(updateHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoHearingDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoCaseDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn404_WhenHearingRequestHasNoPanelDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHasNoRelatedPartyDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
        createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil
                                                                               .relatedPartyMandatoryFieldMissing());
        stubReturn400WhileValidateHearingObject(createHearingRequest);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenNoRoleAssignmentsFound() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        List<RoleAssignmentResource> resourceList = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(resourceList);
        stubReturn200RoleAssignments(USER_ID, roleAssignmentResponse);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors", hasItem(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, USER_ID))))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenRoleAssignmentsDoNotMeetCriteria() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect((status().is(403)))
            .andExpect(jsonPath("$.errors", hasItem(ROLE_ASSIGNMENT_INVALID_ROLE)))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenCaseCannotBeFound() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubSuccessfullyValidateHearingObject(createHearingRequest);
        stubRoleAssignments();
        stubReturn404FromDataStore(CASE_REFERENCE);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors", hasItem(CASE_NOT_FOUND)))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn403WhenRoleAssignmentDoesNotMatchCaseDetails() throws Exception {
        HearingRequest createHearingRequest = new HearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        final CaseDetails caseDetailsIN = TestingUtil.caseDetails();
        final CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        final CaseCategory categorySubType = new CaseCategory();
        categorySubType.setCategoryType("caseSubType");
        categorySubType.setCategoryValue("PROBATE");
        categorySubType.setCategoryParent("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseCategories.add(categorySubType);
        caseDetailsIN.setCaseCategories(caseCategories);
        createHearingRequest.setCaseDetails(caseDetailsIN);
        createHearingRequest.setCaseDetails(caseDetailsIN);
        stubSuccessfullyValidateHearingObject(createHearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId("invalidCaseType")
            .jurisdiction("invalidJurisdiction")
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createHearingRequest)))
            .andExpect(status().is(403))
            .andExpect(jsonPath("$.errors", hasItem(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES)))
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

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn404_WhenDeleteHearingIdIsInValid() throws Exception {
        mockMvc.perform(delete(url + "/2000000001")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200_WhenDeleteHearingIdIsValid() throws Exception {
        DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
        mockMvc.perform(delete(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(deleteHearingRequest)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000000"))
            .andExpect(jsonPath("$.status").value(CANCELLATION_REQUESTED))
            .andExpect(jsonPath("$.versionNumber").value(2))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();

        final Spliterator<CancellationReasonsEntity> spliterator =
                cancellationReasonsRepository.findAll().spliterator();

        assertEquals(2, spliterator.getExactSizeIfKnown());

        assertTrue(StreamSupport.stream(spliterator, false)
                .map(CancellationReasonsEntity::getCancellationReasonType)
                .collect(Collectors.toList())
                .containsAll(CANCELLATION_REASON_CODES));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenDeleteHearingIdIsNonNumeric() throws Exception {
        mockMvc.perform(delete(url + "/200000000P")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenDeleteHearingIdStatusInValid() throws Exception {
        stubFor(WireMock.get(urlMatching("/cases/1111222233335555"))
                    .willReturn(okJson("{\n"
                                           + "\t\"jurisdiction\": \"Test\",\n"
                                           + "\t\"case_type\": \"CaseType\"\n"
                                           + "}")));
        mockMvc.perform(delete(url + "/2000000011")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.deleteHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_DELETE_HEARING_STATUS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenCancellationReasonExceedsMaxLength() throws Exception {
        DeleteHearingRequest hearingRequest = new DeleteHearingRequest();
        hearingRequest.setCancellationReasonCodes(List.of("a".repeat(101)));
        mockMvc.perform(delete(url + "/2000000001")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItems(CANCELLATION_REASON_CODE_MAX_LENGTH_MSG)))
            .andReturn();

        assertFalse(cancellationReasonsRepository.findAll().iterator().hasNext());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenCancellationReasonIsNull() throws Exception {
        DeleteHearingRequest hearingRequest = new DeleteHearingRequest();
        hearingRequest.setCancellationReasonCodes(null);
        mockMvc.perform(delete(url + "/2000000001")
                 .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItems(INVALID_CANCELLATION_REASON_CODE)))
                .andReturn();

        assertFalse(cancellationReasonsRepository.findAll().iterator().hasNext());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenCancellationReasonIsEmpty() throws Exception {
        DeleteHearingRequest hearingRequest = new DeleteHearingRequest();
        hearingRequest.setCancellationReasonCodes(Collections.emptyList());
        mockMvc.perform(delete(url + "/2000000001")
                 .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItems(INVALID_CANCELLATION_REASON_CODE)))
                .andReturn();

        assertFalse(cancellationReasonsRepository.findAll().iterator().hasNext());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenCancellationContainsAnEmptyString() throws Exception {
        DeleteHearingRequest hearingRequest = new DeleteHearingRequest();
        hearingRequest.setCancellationReasonCodes(List.of("reason", ""));
        mockMvc.perform(delete(url + "/2000000001")
                 .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItems(CANCELLATION_REASON_CODE_MAX_LENGTH_MSG)))
                .andReturn();
        assertFalse(cancellationReasonsRepository.findAll().iterator().hasNext());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200_WhenCancellationReasonIsMaxLength() throws Exception {
        DeleteHearingRequest hearingRequest = new DeleteHearingRequest();
        final String cancellationReasonCode = "a".repeat(100);
        hearingRequest.setCancellationReasonCodes(List.of(cancellationReasonCode));
        mockMvc.perform(delete(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000000"))
            .andExpect(jsonPath("$.status").value(CANCELLATION_REQUESTED))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();

        assertEquals(cancellationReasonCode,
                cancellationReasonsRepository.findAll().iterator().next().getCancellationReasonType());
    }



    @Test
    void shouldReturn200_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        mockMvc.perform(get("/hearings/9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    void shouldReturn200_WhenGetHearingStatusIsListed() throws Exception {
        mockMvc.perform(get("/hearings/9856815055686759")
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
            .andExpect(jsonPath("$.hmctsServiceCode").value("TEST"))
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
            .andExpect(jsonPath("$.hmctsServiceCode").value("TEST"))
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
    void shouldReturn400_WhenGetHearingsForListOfCases_NoCaseRefs() throws Exception {
        mockMvc.perform(get("/hearings?ccdCaseRefs=")
                            .param("caseTypeId", CASE_TYPE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenGetHearingsForListOfCases_NoCaseType() throws Exception {
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", "9372710950276233")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn200_WhenGetHearingsForListOfCasesForInvalidCaseRef() throws Exception {
        List<String> caseRefs = Arrays.asList("123456");
        stubReturn200ForAllCasesFromDataStore(caseRefs, new ArrayList<>());
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", "123456")
                            .param("caseTypeId", CASE_TYPE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForListOfCasesForOneCaseRef() throws Exception {
        List<String> caseRefs = Arrays.asList("9372710950276233", "9372710950276239");
        String caseRefsParam = caseRefs.stream().collect(Collectors.joining(","));
        stubReturn200ForAllCasesFromDataStore(caseRefs, caseRefs);
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", caseRefsParam)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("caseTypeId", CASE_TYPE)
                            .param("status", "HEARING_REQUESTED"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.*", hasSize(2)))
            .andExpect(jsonPath("$[0].caseRef").value("9372710950276233"))
            .andExpect(jsonPath("$[1].caseRef").value("9372710950276239"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForListOfCasesForMoreThanDefaultSizeCaseRef() throws Exception {
        List<String> caseRefs = new ArrayList<>(Collections.nCopies(11,  "9372710950276239"));
        String caseRefsParam = caseRefs.stream().collect(Collectors.joining(","));
        stubReturn200ForAllCasesFromDataStore(caseRefs, caseRefs);
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", caseRefsParam)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("caseTypeId", CASE_TYPE)
                            .param("status", "HEARING_REQUESTED"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.*", hasSize(11)))
            .andExpect(jsonPath("$[0].caseRef").value("9372710950276239"))
            .andExpect(jsonPath("$[10].caseRef").value("9372710950276239"))
            .andReturn();
    }


    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForListOfCasesForCaseRef_Listed() throws Exception {
        List<String> caseRefs = Arrays.asList("9372710950276233","9856815055686759");
        String caseRefsParam = caseRefs.stream().collect(Collectors.joining(","));
        stubReturn200ForAllCasesFromDataStore(caseRefs, Arrays.asList(caseRefs.get(1)));
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", caseRefsParam)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("caseTypeId", CASE_TYPE)
                            .param("status", "LISTED"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.*", hasSize(1)))
            .andExpect(jsonPath("$[0].caseRef").value("9856815055686759"))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForListOfCasesForCaseRef_NotListed() throws Exception {
        List<String> caseRefs = Arrays.asList("9372710950276233","9856815055686759");
        String caseRefsParam = caseRefs.stream().collect(Collectors.joining(","));
        stubReturn200ForAllCasesFromDataStore(caseRefs, Arrays.asList(caseRefs.get(0)));
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", caseRefsParam)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("caseTypeId", CASE_TYPE)
                            .param("status", "HEARING_REQUESTED"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.*", hasSize(1)))
            .andExpect(jsonPath("$[0].caseRef").value("9372710950276233"))
            .andReturn();
    }

    @Test
    void shouldReturn400_WhenGetHearingsForListOfCases_CaseTypeIsEmpty() throws Exception {
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", "9372710950276233")
                            .param("caseTypeId", "")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn200_WhenGetHearingsForListOfCasesForCaseRefNotInDB() throws Exception {
        List<String> caseRefs = Arrays.asList("9372710950276245");
        stubReturn200ForAllCasesFromDataStore(caseRefs, new ArrayList<>());
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", "9372710950276245")
                            .param("caseTypeId", CASE_TYPE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldReturn200_WhenGetHearingsForListOfCasesForCaseRef_NoStatus() throws Exception {
        List<String> caseRefs = Arrays.asList("9372710950276233","9856815055686759");
        String caseRefsParam = caseRefs.stream().collect(Collectors.joining(","));
        stubReturn200ForAllCasesFromDataStore(caseRefs,Arrays.asList(caseRefs.get(0)));
        mockMvc.perform(get("/hearings")
                            .param("ccdCaseRefs", caseRefsParam)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("caseTypeId", CASE_TYPE))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.*", hasSize(1)))
            .andExpect(jsonPath("$[0].caseRef").value("9372710950276233"))
            .andReturn();
    }



    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestIsValid() throws Exception {
        stubFor(WireMock.get(urlMatching("/cases/1111222233334444"))
                    .willReturn(okJson("{\n"
                                           + "\t\"jurisdiction\": \"Test\",\n"
                                           + "\t\"case_type\": \"CaseType\"\n"
                                           + "}")));
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.validUpdateHearingRequest())))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000000"))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();
        assertChangeReasons();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestContainsValidPartyDetails() throws Exception {
        stubRoleAssignments();
        UpdateHearingRequest hearingRequest = TestingUtil.validUpdateHearingRequest();
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getCaseDetails().setCaseRef("9856815055686759");
        mockMvc.perform(put(url + "/2000000012")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
        assertChangeReasons();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenUpdateHearingRequestHasNoAmendReasonCodes() throws Exception {
        UpdateHearingRequest updateHearingRequest = TestingUtil.validUpdateHearingRequest();
        updateHearingRequest.getHearingDetails().setAmendReasonCodes(Collections.emptyList());
        updateHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(updateHearingRequest);
        mockMvc.perform(put(url + "/2000000000")
                 .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateHearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(INVALID_AMEND_REASON_CODE)))
                .andReturn();

        assertFalse(changeReasonsRepository.findAll().iterator().hasNext());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenUpdateHearingRequestHasEmptyStringAmendReasonCodes() throws Exception {
        UpdateHearingRequest updateHearingRequest = TestingUtil.validUpdateHearingRequest();
        updateHearingRequest.getHearingDetails().setAmendReasonCodes(List.of("", "reason"));
        updateHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        stubReturn400WhileValidateHearingObject(updateHearingRequest);
        mockMvc.perform(put(url + "/2000000000")
                .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateHearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem(AMEND_REASON_CODE_MAX_LENGTH)))
                .andReturn();

        assertFalse(changeReasonsRepository.findAll().iterator().hasNext());
    }

    private void assertChangeReasons() {
        final Spliterator<ChangeReasonsEntity> spliterator = changeReasonsRepository.findAll().spliterator();
        assertEquals(2, spliterator.estimateSize());
        assertTrue(StreamSupport.stream(spliterator, false)
                .map(ChangeReasonsEntity::getChangeReasonType).collect(Collectors.toList())
                .containsAll(List.of("reason 1", "reason 2")));
    }

    @Test
    void shouldReturn404WhenUpdateHearingIdDoesNotMatchHearingIdInDB() throws Exception {
        mockMvc.perform(put(url + "/2000000001")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenUpdateHearingIdIsInvalid() throws Exception {
        mockMvc.perform(put(url + "/3000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
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
        RequestDetails updateRequestDetails = new RequestDetails();
        hearingRequest.setRequestDetails(updateRequestDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItems(VERSION_NUMBER_NULL_EMPTY)))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingDetailsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        HearingDetails hearingDetails = new HearingDetails();
        hearingRequest.setHearingDetails(hearingDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(8)))
            .andExpect(jsonPath("$.errors", hasItems(AUTO_LIST_FLAG_NULL_EMPTY, HEARING_TYPE_NULL_EMPTY,
                                                      DURATION_EMPTY, HEARING_PRIORITY_TYPE,
                                                     HEARING_LOCATION_EMPTY, INVALID_HEARING_LOCATION,
                                                     INVALID_PANEL_REQUIREMENTS,
                                                     HEARING_CHANNEL_EMPTY
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingDetailsAreInvalid() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().setHearingType("a".repeat(41));
        hearingRequest.getHearingDetails().setListingAutoChangeReasonCode("a".repeat(71));
        hearingRequest.getHearingDetails().setDuration(-1);
        hearingRequest.getHearingDetails().setAmendReasonCodes(List.of("a".repeat(71)));
        List<String> nonStandardHearingDurationReasonsList = Collections.singletonList("a".repeat(71));
        hearingRequest.getHearingDetails().setNonStandardHearingDurationReasons(nonStandardHearingDurationReasonsList);
        hearingRequest.getHearingDetails().setHearingPriorityType("a".repeat(61));
        hearingRequest.getHearingDetails().setNumberOfPhysicalAttendees(-1);
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationType("invalid enum");
        List<HearingLocation> hearingLocationList = Collections.singletonList(hearingLocation);
        hearingRequest.getHearingDetails().setHearingLocations(hearingLocationList);
        List<String> facilitiesRequiredList = Collections.singletonList("a".repeat(71));
        hearingRequest.getHearingDetails().setFacilitiesRequired(facilitiesRequiredList);
        hearingRequest.getHearingDetails().setListingComments("a".repeat(2001));
        hearingRequest.getHearingDetails().setHearingRequester("a".repeat(61));
        hearingRequest.getHearingDetails().setLeadJudgeContractType("a".repeat(71));
        hearingRequest.getHearingDetails().setHearingChannels(null);
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(23)))
            .andExpect(jsonPath("$.errors", hasItems(HEARING_TYPE_MAX_LENGTH, DURATION_MIN_VALUE,
                                                     NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG,
                                                     HEARING_PRIORITY_TYPE_MAX_LENGTH,
                                                     NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE,
                                                     FACILITIES_REQUIRED_MAX_LENGTH_MSG, LISTING_COMMENTS_MAX_LENGTH,
                                                     HEARING_REQUESTER_MAX_LENGTH, LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH,
                                                     LOCATION_ID_EMPTY, "Unsupported type for locationType",
                                                     ROLE_TYPE_MAX_LENGTH_MSG, AUTHORISATION_TYPE_MAX_LENGTH_MSG,
                                                     AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG,
                                                     PANEL_SPECIALISMS_MAX_LENGTH_MSG, MEMBER_ID_EMPTY,
                                                     MEMBER_ID_MAX_LENGTH, MEMBER_TYPE_MAX_LENGTH,
                                                     "Unsupported type for requirementType",
                                                     AMEND_REASON_CODE_MAX_LENGTH,
                                                     HEARING_CHANNEL_EMPTY,
                                                     LISTING_REASON_CODE_MAX_LENGTH,
                                                     "Unsupported type or value for listingAutoChangeReasonCode"
            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenCaseDetailsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        hearingRequest.setCaseDetails(caseDetails);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(10)))
            .andExpect(jsonPath("$.errors", hasItems(HMCTS_SERVICE_CODE_EMPTY_INVALID, CASE_REF_EMPTY,
                                                     CASE_DEEP_LINK_EMPTY,
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
        logger.info("request body: {}", objectMapper.writeValueAsString(hearingRequest));
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtDefinition)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(11)))
            .andExpect(jsonPath("$.errors", hasItems(HMCTS_SERVICE_CODE_EMPTY_INVALID, INVALID_CASE_REFERENCE,
                                                     EXTERNAL_CASE_REFERENCE_MAX_LENGTH, CASE_DEEP_LINK_MAX_LENGTH,
                                                     CASE_DEEP_LINK_INVALID, HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH,
                                                     PUBLIC_CASE_NAME_MAX_LENGTH,
                                                     CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH, CATEGORY_VALUE,
                                                     "Unsupported type for categoryType", CATEGORY_TYPE_EMPTY

            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenPartyDetailsNull() throws Exception {
        IndividualDetails individualDetails = new IndividualDetails();
        RelatedParty relatedParty = new RelatedParty();
        individualDetails.setRelatedParties(Collections.singletonList(relatedParty));
        OrganisationDetails organisationDetails = new OrganisationDetails();
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        List<UnavailabilityDow> unavailabilityDowList = Collections.singletonList(unavailabilityDow);
        UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
        List<UnavailabilityRanges> unavailabilityRangesList = Collections.singletonList(unavailabilityRanges);
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(unavailabilityRangesList);
        partyDetails.setUnavailabilityDow(unavailabilityDowList);
        List<PartyDetails> listPartyDetails = new ArrayList<>();
        listPartyDetails.add(partyDetails);
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.setPartyDetails(listPartyDetails);
        logger.info("request body: {}", objectMapper.writeValueAsString(hearingRequest));
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(13)))
            .andExpect(jsonPath("$.errors", hasItems(PARTY_DETAILS_NULL_EMPTY,
                                                     "Unsupported type for partyType", UNAVAILABLE_FROM_DATE_EMPTY,
                                                     UNAVAILABLE_TO_DATE_EMPTY, "Unsupported type for dow",
                                                     NAME_NULL_EMPTY,
                                                     ORGANISATION_TYPE_NULL_EMPTY,
                                                     FIRST_NAME_EMPTY, LAST_NAME_EMPTY, RELATED_PARTY_EMPTY,
                                                     RELATIONSHIP_TYPE_EMPTY, PARTY_ROLE_EMPTY, PARTY_TYPE_EMPTY
            )))
            .andReturn();
    }

    @Test
    void shouldReturn400WhenPartyDetailsInvalid() throws Exception {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("a".repeat(41));
        partyDetails.setPartyRole("a".repeat(41));
        partyDetails.setPartyType("IND");
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("a".repeat(41));
        individualDetails.setFirstName("a".repeat(101));
        individualDetails.setLastName("a".repeat(731));
        individualDetails.setPreferredHearingChannel("a".repeat(71));
        individualDetails.setInterpreterLanguage("a".repeat(11));
        individualDetails.setReasonableAdjustments(Collections.singletonList("a".repeat(11)));
        individualDetails.setVulnerabilityDetails("a".repeat(2001));
        individualDetails.setHearingChannelEmail(List.of("a".repeat(121)));
        individualDetails.setHearingChannelPhone(List.of("a".repeat(31)));
        individualDetails.setOtherReasonableAdjustmentDetails("a".repeat(3001));
        individualDetails.setCustodyStatus("a".repeat(81));
        RelatedParty relatedParty = new RelatedParty();
        relatedParty.setRelatedPartyID("a".repeat(16));
        relatedParty.setRelationshipType("a".repeat(11));
        individualDetails.setRelatedParties(Collections.singletonList(relatedParty));
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("a".repeat(2001));
        organisationDetails.setOrganisationType("a".repeat(61));
        organisationDetails.setCftOrganisationID("a".repeat(61));
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        UnavailabilityDow unavailabilityDowMonday = new UnavailabilityDow();
        unavailabilityDowMonday.setDow("MONDAY");
        partyDetails.setUnavailabilityDow(List.of(unavailabilityDowMonday));
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.setPartyDetails(Collections.singletonList(partyDetails));

        final String unexpectedTypeForDow = "Unsupported type for dow";
        logger.info("request body: {}", objectMapper.writeValueAsString(hearingRequest));
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(21)))
            .andExpect(jsonPath("$.errors", hasItems(PARTY_DETAILS_MAX_LENGTH, PARTY_ROLE_MAX_LENGTH,
                                                     TITLE_MAX_LENGTH, FIRST_NAME_MAX_LENGTH, LAST_NAME_MAX_LENGTH,
                                                     PREFERRED_HEARING_CHANNEL_MAX_LENGTH,
                                                     INTERPRETER_LANGUAGE_MAX_LENGTH,
                                                     REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG,
                                                     VULNERABLE_DETAILS_MAX_LENGTH, HEARING_CHANNEL_EMAIL_MAX_LENGTH,
                                                     HEARING_CHANNEL_PHONE_MAX_LENGTH, HEARING_CHANNEL_PHONE_INVALID,
                                                     RELATED_PARTY_MAX_LENGTH, RELATIONSHIP_TYPE_MAX_LENGTH,
                                                     NAME_MAX_LENGTH, ORGANISATION_TYPE_MAX_LENGTH,
                                                     CFT_ORG_ID_MAX_LENGTH, HEARING_CHANNEL_EMAIL_INVALID,
                                                     CUSTODY_STATUS_LENGTH, OTHER_REASON_LENGTH, unexpectedTypeForDow
            )))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingWindowFieldsAreNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(null);
        hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(null);
        hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_WINDOW_EMPTY_NULL))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingWindowHasUnexpectedFieldsInGroup() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(LocalDate.parse("2017-03-01"));
        hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(LocalDate.parse("2017-03-01"));
        hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(LocalDateTime.now());
        mockMvc.perform(put(url + "/2000000000")
                 .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_WINDOW_DETAILS_ARE_INVALID))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenHearingChannelFieldIsNull() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getHearingDetails().setHearingChannels(null);
        mockMvc.perform(put(url + "/2000000000")
                 .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_CHANNEL_EMPTY))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400WhenPartyIndividualAndOrgDetailsNull() throws Exception {
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        request.setPartyDetails(TestingUtil.partyDetails());
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
        stubRoleAssignments();
        UpdateHearingRequest request = TestingUtil.updateHearingRequest();
        request.setPartyDetails(TestingUtil.partyDetails());
        request.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        request.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
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
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(TestingUtil.updateHearingRequest())))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_PUT_HEARING_STATUS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenUpdateHearingRequestHearingPartyTechPartyId_NotFound() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.validUpdateHearingRequest();
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(0).getIndividualDetails().getRelatedParties()
            .get(0).setRelatedPartyID("unknown");
        hearingRequest.getPartyDetails().get(0).getIndividualDetails().getRelatedParties()
            .get(0).setRelationshipType("a".repeat(10));
        hearingRequest.getCaseDetails().setCaseRef("9856815055686759");
        mockMvc.perform(put(url + "/2000000012")
                 .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("Cannot find unique PartyID with value unknown")))
                .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UPDATE_HEARINGS_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestIsValidWith2PartyDetailsAndOrgDetail() throws Exception {
        stubRoleAssignments();
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequestWithPartyDetails(false);
        hearingRequest.getCaseDetails().setCaseRef("9372710950276233");
        mockMvc.perform(put(url + "/2000000024")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000024"))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UPDATE_HEARINGS_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestIsValidWith2PartyDetailsAndOrgDetailWhereOrgIdIsNull()
        throws Exception {
        UpdateHearingRequest hearingRequest =
            TestingUtil.updateHearingRequestWithPartyDetails(true);
        hearingRequest.getCaseDetails().setCaseRef("9372710950276233");
        mockMvc.perform(put(url + "/2000000024")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.hearingRequestID").value("2000000024"))
            .andExpect(jsonPath("$.timeStamp").value(IsNull.notNullValue()))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn400_WhenCaseRefHasInvalidFormat_UpdateHearing() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        hearingRequest.getCaseDetails().setCaseRef("1111222233334445");
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_CASE_REFERENCE))))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenCaseRefHasInvalidFormat_CreateHearing() throws Exception {
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        hearingRequest.getCaseDetails().setCaseRef("1111222233334445");
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_CASE_REFERENCE))))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenHearingRequestHasValidData() throws Exception {
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenCreateHearingRequestHasValidDurations() throws Exception {
        testPostForDurationValue(476, 480);
        testPostForDurationValue(476, 480);
        testPostForDurationValue(485, 485);
    }

    private void testPostForDurationValue(Integer duration, Integer expectedDuration) throws Exception {
        val hearingRequest = getHearingRequest("P1");
        hearingRequest.getHearingDetails().setDuration(duration);
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        val result = mockMvc.perform(post(url)
                                         .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                                         .contentType(MediaType.APPLICATION_JSON_VALUE)
                                         .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();

        val response = result.getResponse().getContentAsString();
        val hearingId = JsonPath.parse(response).read("$.hearingRequestID").toString();
        assertDurationForHearing(hearingId, expectedDuration);
    }

    private HearingRequest getHearingRequest(String partyId) {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.getValidCaseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        hearingRequest.getHearingDetails().setListingComments("a".repeat(2000));
        hearingRequest.getPartyDetails().get(0).getIndividualDetails().getRelatedParties()
            .get(0).setRelatedPartyID(partyId);
        hearingRequest.getPartyDetails().get(0).getIndividualDetails().getRelatedParties()
            .get(0).setRelationshipType("a".repeat(10));
        return hearingRequest;
    }

    void assertDurationForHearing(String hearingId, Integer expectedDuration) throws Exception {
        mockMvc.perform(get("/hearing/" + hearingId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.hearingDetails.duration").value(expectedDuration))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UPDATE_HEARINGS_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestWithValidDurations() throws Exception {
        testUpdateHearingRequestWithValidDurations(476, 480);
    }

    private void testUpdateHearingRequestWithValidDurations(Integer duration,
                                                            Integer expectedDuration) throws Exception {

        val hearingId = "2000000024";
        val hearingRequest =
            TestingUtil.updateHearingRequestWithPartyDetails(true);

        hearingRequest.getHearingDetails().setDuration(duration);
        hearingRequest.getCaseDetails().setCaseRef("9372710950276233");

        mockMvc.perform(put(url + "/" + hearingId)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.hearingRequestID").value(hearingId))
            .andReturn();

        assertDurationForHearing(hearingId, expectedDuration);
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenHearingRequestHearingPartyTechPartyId_NotFound() throws Exception {
        HearingRequest hearingRequest = getHearingRequest("unknown");
        stubSuccessfullyValidateHearingObject(hearingRequest);
        mockMvc.perform(post(url)
                 .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hearingRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errors", hasItem("Cannot find unique PartyID with value unknown")))
                .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenDeploymentIdEnabledWithValue() throws Exception {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .header(HMCTS_DEPLOYMENT_ID, "TEST")
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenDeploymentIdEnabled_NoDeploymentId() throws Exception {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn201_WhenDeploymentIdEnabledFalse_NoDeploymentId() throws Exception {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", false);
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenDeploymentIdEnabledAndGreaterThanMaxLength() throws Exception {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .header(HMCTS_DEPLOYMENT_ID, "a".repeat(41))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem(HMCTS_DEPLOYMENT_ID_MAX_LENGTH)))
            .andReturn();
    }

    @Test
    @Sql(DELETE_HEARING_DATA_SCRIPT)
    void shouldReturn400_WhenDeploymentIdEnabledFalseWithDeploymentID() throws Exception {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", false);
        val hearingRequest = getHearingRequest("P1");
        stubSuccessfullyValidateHearingObject(hearingRequest);

        stubRoleAssignments();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        stubReturn200CaseDetailsByCaseId(CASE_REFERENCE, caseDetails);
        mockMvc.perform(post(url)
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .header(HMCTS_DEPLOYMENT_ID, "ABA1")
                            .header(HMCTS_DEPLOYMENT_ID, "TEST")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem(HMCTS_DEPLOYMENT_ID_NOT_REQUIRED)))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn404WhenHearingIdNotAvailableHearingCompletion() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000001")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(404))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem(("001 No such id: 2000000001"))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn404WhenHearingIdIsNotValidHearingCompletion() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/30000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((INVALID_HEARING_ID_DETAILS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn404WhenHearingStatusIsNotVaidHearingCompletion() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000009")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_ACTUALS_INVALID_STATUS))))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn404WhenHearingStatusListedAndMinStartDateIsBeforeTodayHearingCompletion() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000010")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn404WhenActualHearingMissingForHearingResponseHearingCompletion() throws Exception {
        stubFor(WireMock.get(urlMatching("/cases/9372710950276233"))
                    .willReturn(okJson("{\n"
                                           + "\t\"jurisdiction\": \"Test\",\n"
                                           + "\t\"case_type\": \"CaseType\"\n"
                                           + "}")));
        mockMvc.perform(post(hearingCompletion + "/2000000012")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem((HEARING_ACTUALS_MISSING_HEARING_OUTCOME))))
            .andReturn();
    }


    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn200WhenActualHearingDayMissingForResultTypeAdjourned() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000013")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn200WhenActualHearingDayMissingForResultTypeCompleted() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000015")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldReturn200WhenActualHearingDayExistsForCanceledResultTypeResponseHearingCompletion() throws Exception {
        mockMvc.perform(post(hearingCompletion + "/2000000014")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, CASE_HEARING_ACTUAL_HEARING})
    void shouldUpdateHearingCompletion() throws Exception {
        stubRoleAssignments();
        mockMvc.perform(post(hearingCompletion + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn200WhenHearingWindowIsNotPresent() throws Exception {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequestWithoutHearingWindow(1);
        mockMvc.perform(put(url + "/2000000000")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void shouldReturn201WhenUpdateHearingRequestContainsValidPartyDetails_reasonable_adjustments() throws Exception {
        stubRoleAssignments();
        UpdateHearingRequest hearingRequest = TestingUtil.validUpdateHearingRequest();
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        IndividualDetails individualDetails = TestingUtil.individualDetails();
        individualDetails.setOtherReasonableAdjustmentDetails("a".repeat(3000));
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(individualDetails);
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(individualDetails);
        hearingRequest.getCaseDetails().setCaseRef("9856815055686759");
        mockMvc.perform(put(url + "/2000000012")
                            .header(SERVICE_AUTHORIZATION, serviceJwtXuiWeb)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(hearingRequest)))
            .andExpect(status().is(201))
            .andReturn();
        assertChangeReasons();
    }

    @Nested
    class GetHearingsForListOfCases {

        @Test
        void shouldFailValidationNoCaseTypeParameter() throws Exception {
            String request = """
                {
                    "pageSize": 10,
                    "offset": 0,
                    "caseReferences": [
                        {"caseReference": "1234123412341234"}
                    ]
                }""";

            mockMvc.perform(post("/hearings")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Required parameter 'caseTypeId' is not present."));
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("bodyValidationErrors")
        void shouldFailValidationBody(String request, List<String> expectedErrors) throws Exception {
            mockMvc.perform(post("/hearings?caseTypeId=" + CASE_TYPE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())))
                .andExpect(jsonPath("$.errors", hasItems(expectedErrors.toArray())));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_REQUEST_PAGINATED_DATA_SCRIPT})
        void shouldGetHearingsForListOfCases() throws Exception {
            List<String> caseReferences = List.of("1234123412341234", "5678567856785678");
            stubReturn200ForAllCasesFromDataStorePaginated(10, 0, caseReferences, CASE_TYPE, caseReferences);

            String request = createGetHearingRequest(10, 0, caseReferences);

            mockMvc.perform(post("/hearings?caseTypeId=" + CASE_TYPE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].caseRef").value("1234123412341234"))
                .andExpect(jsonPath("$[0].caseHearings", hasSize(1)))
                .andExpect(jsonPath("$[0].caseHearings[0].hearingID").value("2000000000"));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT})
        void shouldReturnNoHearingsWhenNoCasesFound() throws Exception {
            List<String> caseReferences = List.of("5678567856785678");
            stubReturn200ForAllCasesFromDataStorePaginated(5, 0, caseReferences, CASE_TYPE, Collections.emptyList());

            String request = createGetHearingRequest(5, 0, caseReferences);

            mockMvc.perform(post("/hearings?caseTypeId=" + CASE_TYPE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT})
        void shouldReturnNoHearingsWhenNoHearingsForCaseFound() throws Exception {
            List<String> caseReferences = List.of("5678567856785678");
            stubReturn200ForAllCasesFromDataStorePaginated(5, 1, caseReferences, CASE_TYPE, caseReferences);

            String request = createGetHearingRequest(5, 1, caseReferences);

            mockMvc.perform(post("/hearings?caseTypeId=" + CASE_TYPE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));
        }

        private String createGetHearingRequest(int pageSize, int offset, List<String> caseReferences) {
            String caseReferenceItems =
                caseReferences == null || caseReferences.isEmpty() ? "" :
                    caseReferences.stream()
                        .map(caseRef -> "{\"caseReference\": \"" + caseRef + "\"}")
                        .collect(Collectors.joining(", "));

            return """
                {
                    "pageSize": %d,
                    "offset": %d,
                    "caseReferences": [%s]
                }""".formatted(pageSize, offset, caseReferenceItems);
        }

        private static Stream<Arguments> bodyValidationErrors() {
            return Stream.of(
                arguments(
                    named("HasNoPageSize",
                          """
                          {
                              "offset": 0,
                              "caseReferences": [
                                  {"caseReference": "1234123412341234"}
                              ]
                          }"""),
                    List.of("Page size is mandatory")
                ),
                arguments(
                    named("PageSizeNotPositive",
                          """
                          {
                              "pageSize": 0,
                              "offset": 0,
                              "caseReferences": [
                                  {"caseReference": "1234123412341234"}
                              ]
                          }"""),
                    List.of("Page size can't be less than one")
                ),
                arguments(
                    named("HasNoOffset",
                          """
                          {
                              "pageSize": 10,
                              "caseReferences": [
                                  {"caseReference": "1234123412341234"}
                              ]
                          }"""),
                    List.of("Offset is mandatory")
                ),
                arguments(
                    named("OffsetLessThanZero",
                          """
                          {
                              "pageSize": 10,
                              "offset": -1,
                              "caseReferences": [
                                  {"caseReference": "1234123412341234"}
                              ]
                          }"""),
                    List.of("Offset can't be less than 0")
                ),
                arguments(
                    named("HasNoCaseReferences",
                          """
                          {
                              "pageSize": 10,
                              "offset": 0
                          }"""),
                    List.of("At least one case reference must be provided")
                ),
                arguments(
                    named("CaseReferencesEmpty",
                          """
                          {
                              "pageSize": 10,
                              "offset": 0,
                              "caseReferences": []
                          }"""),
                    List.of("At least one case reference must be provided")
                ),
                arguments(
                    named("CaseReferencesCaseReferenceBlank",
                          """
                          {
                              "pageSize": 10,
                              "offset": 0,
                              "caseReferences": [
                                  {"caseReference": ""}
                              ]
                          }"""
                    ),
                    List.of("Case ref has invalid length", "Case ref can not be empty")
                ),
                arguments(
                    named("CaseReferencesCaseReferenceInvalidLength",
                          """
                          {
                              "pageSize": 10,
                              "offset": 0,
                              "caseReferences": [
                                  {"caseReference": "1"}
                              ]
                          }"""),
                    List.of("Case ref has invalid length")
                )
            );
        }
    }

    private final String serviceJwtDefinition = generateDummyS2SToken("ccd_definition");
    private final String serviceJwtXuiWeb = generateDummyS2SToken("xui_webapp");

}
