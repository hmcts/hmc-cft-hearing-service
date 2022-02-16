package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToQueueConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.GetHearingsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiDeleteHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiSubmitHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.repository.CancellationReasonsRepository;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_RELATED_PARTY_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_DOW_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_RANGES_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class HearingManagementServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(HearingManagementServiceTest.class);

    @InjectMocks
    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    private DataStoreRepository dataStoreRepository;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private HearingMapper hearingMapper;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @Mock
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Mock
    private ObjectMapperService objectMapperService;

    @Mock
    private GetHearingsResponseMapper getHearingsResponseMapper;

    @Mock
    private GetHearingResponseMapper getHearingResponseMapper;

    @Mock
    HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;

    @Mock
    HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper;

    @Mock
    CancellationReasonsRepository cancellationReasonsRepository;

    @Mock
    MessageSenderToQueueConfiguration messageSenderToQueueConfiguration;

    JsonNode jsonNode = mock(JsonNode.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingManagementService =
            new HearingManagementServiceImpl(
                roleAssignmentService,
                securityUtils,
                dataStoreRepository,
                hearingRepository,
                hearingMapper,
                caseHearingRequestRepository,
                cancellationReasonsRepository,
                hmiSubmitHearingRequestMapper,
                getHearingsResponseMapper,
                getHearingResponseMapper,
                messageSenderToTopicConfiguration,
                objectMapperService,
                hmiDeleteHearingRequestMapper,
                messageSenderToQueueConfiguration
            );
    }

    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String USER_ID = "UserId";
    public static final String ROLE_NAME = "Hearing Manage";
    public static final String ROLE_TYPE = "ORGANISATION";

    @Nested
    @DisplayName("SendResponseToTopic")
    class SendResponseToTopic {
        @Test
        void shouldVerifySubsequentCalls() throws JsonProcessingException {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            when(objectMapperService.convertObjectToJsonNode(json)).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(Mockito.any());
            hearingManagementService.sendResponse(json);
            verify(objectMapperService, times(1)).convertObjectToJsonNode(any());
            verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any());
        }
    }

    @Nested
    @DisplayName("getHearing")
    class GetHearing {
        @Test
        void shouldFailWithInvalidHearingId() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> {
                hearingManagementService.getHearingRequest(2000000000L, true);
            });
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidHearingIdFormat() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(1L);

            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .getHearingRequest(1L, true));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void shouldPassWithValidHearingId() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            hearingManagementService.getHearingRequest(2000000000L, true);
            verify(hearingRepository).existsById(2000000000L);
        }

        @Test
        void shouldPassWithValidHearingIdInDb() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearing));
            hearingManagementService.getHearingRequest(2000000000L, false);
            verify(hearingRepository).existsById(2000000000L);
        }

        @Test
        void shouldFailWithInvalidHearingIdForGetHearing() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000010L);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
                .getHearingRequest(2000000010L, false));
            assertEquals("No hearing found for reference: 2000000010", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("saveHearing")
    class SaveHearing {
        @Test
        void shouldFailAsHearingWindowDetailsNotPresent() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.getHearingDetails().getHearingWindow().setHearingWindowStartDateRange(null);
            createHearingRequest.getHearingDetails().getHearingWindow().setHearingWindowEndDateRange(null);
            createHearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals("Hearing window details are required", exception.getMessage());
        }

        @Test
        void shouldFailIfNullCreateHearingRequest() {
            CreateHearingRequest createHearingRequest = null;
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailAsDetailsNotPresent() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void shouldPassWithHearing_Case_Request_Details_Valid() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            given(hearingMapper.modelToEntity(createHearingRequest)).willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
            assertEquals(VERSION_NUMBER, response.getVersionNumber());
            assertEquals(HEARING_STATUS, response.getStatus());
            assertNotNull(response.getHearingRequestId());
        }

        @Test
        void shouldPassWithHearing_Case_Request_Party_Details_Valid() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            given(hearingMapper.modelToEntity(createHearingRequest)).willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
            assertEquals(VERSION_NUMBER, response.getVersionNumber());
            assertEquals(HEARING_STATUS, response.getStatus());
            assertNotNull(response.getHearingRequestId());

        }

        @Test
        void shouldPassWithRelatedParty_Details_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.getPartyDetails().get(1).setIndividualDetails(
                TestingUtil.individualWithoutRelatedPartyDetails());
            given(hearingMapper.modelToEntity(createHearingRequest)).willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
            assertEquals(VERSION_NUMBER, response.getVersionNumber());
            assertEquals(HEARING_STATUS, response.getStatus());
            assertNotNull(response.getHearingRequestId());

        }

        @Test
        void shouldPassWithRelatedParty_Details_Not_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            given(hearingMapper.modelToEntity(createHearingRequest)).willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(createHearingRequest);
            assertEquals(VERSION_NUMBER, response.getVersionNumber());
            assertEquals(HEARING_STATUS, response.getStatus());
            assertNotNull(response.getHearingRequestId());
        }

        @Test
        void shouldFailWithParty_Details_InValid_Org_Individual_details_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            List<PartyDetails> partyDetails = TestingUtil.partyDetails();
            partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.setPartyDetails(partyDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
        }

        @Test
        void shouldFailWithParty_Details_Invalid_Dow_details_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            List<PartyDetails> partyDetails = TestingUtil.partyDetails();
            partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.get(0).setUnavailabilityDow(new ArrayList<>());
            createHearingRequest.setPartyDetails(partyDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals("Unavailability DOW details should be present", exception.getMessage());
        }

        @Test
        void shouldFailWithParty_Details_Invalid_UnavailabilityRange_details_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            List<PartyDetails> partyDetails = TestingUtil.partyDetails();
            partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.get(0).setUnavailabilityRanges(new ArrayList<>());
            createHearingRequest.setPartyDetails(partyDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals("Unavailability range details should be present", exception.getMessage());

        }

        @Test
        void shouldFailWithParty_Details_Invalid_UnavailabilityDow_details_Present() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            List<PartyDetails> partyDetails = TestingUtil.partyDetails();
            partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
            List<UnavailabilityDow> lstUnavailabilityDow = new ArrayList<>();
            partyDetails.get(0).setUnavailabilityDow(lstUnavailabilityDow);
            createHearingRequest.setPartyDetails(partyDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(createHearingRequest));
            assertEquals(INVALID_UNAVAILABILITY_DOW_DETAILS, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("VerifyAccess")
    class VerifyAccess {
        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesCaseJurisdictionAndCaseTypeId() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesOnlyCaseJurisdiction() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId("different casetypeid")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesOnlyCaseTypeId() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("different Jurisdiction")
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndNoJurisdictionOrCaseTypeIdGivenInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionAndCaseTypeIdIsEmptyInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.empty())
                .caseType(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("different Jurisdiction")
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsEmptyInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.empty())
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsEmptyInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsNullInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsNullInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsEmptyAndJurisdictionIsNullInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .caseType(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsNullAndJurisdictionIsEmptyInRoleAssignment() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenNoRoleAssignmentsReturned() {
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, USER_ID), exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenFilteredRoleAssignmentsListEmpty() {
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName("divorce")
                .roleType(ROLE_TYPE)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ROLE, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenRoleAssignmentDoesNotMatchCaseDetails() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsNull() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsEmpty() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .jurisdiction(Optional.of(JURISDICTION))
                .caseType(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsNull() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .caseType(Optional.of(CASE_TYPE))
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsEmpty() {
            RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
                .caseType(Optional.of(CASE_TYPE))
                .jurisdiction(Optional.empty())
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(ROLE_NAME)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> hearingManagementService
                .verifyAccess(CASE_REFERENCE));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteHearing")
    class DeleteHearing {
        @Test
        void deleteHearingRequestShouldPassWithValidDetails() {
            final long hearingId = 2000000000L;
            CaseHearingRequestEntity entity = TestingUtil.caseHearingRequestEntity();
            final int versionNumber = entity.getVersionNumber();
            entity.setCaseHearingID(1L);
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("UPDATE_SUBMITTED");
            HearingEntity hearingEntity = generateHearingEntity(
                hearingId,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);
            when(caseHearingRequestRepository.getCaseHearing(hearingId)).thenReturn(entity);

            HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
                hearingId, TestingUtil.deleteHearingRequest());
            assertEquals(versionNumber + 1, hearingResponse.getVersionNumber());
            assertEquals(CANCELLATION_REQUESTED, hearingResponse.getStatus());
            assertNotNull(hearingResponse.getHearingRequestId());
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getVersionNumber(hearingId);
        }

        @Test
        void deleteHearingRequestShouldPassWithValidStatus() {
            final long hearingId = 2000000000L;
            HearingEntity hearingEntity = generateHearingEntity(
                hearingId,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1
            );
            final int versionNumber = hearingEntity.getCaseHearingRequest().getVersionNumber();
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(1);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("UPDATE_SUBMITTED");
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);
            HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
                hearingId, TestingUtil.deleteHearingRequest());
            assertEquals(CANCELLATION_REQUESTED, hearingResponse.getStatus());
            assertEquals(versionNumber + 1, hearingResponse.getVersionNumber());
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getVersionNumber(hearingId);
        }

        @Test
        void deleteHearingRequestShouldFailWithInvalidStatus() {
            final long hearingId = 2000000000L;
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            final int versionNumber = deleteHearingRequest.getVersionNumber();
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("UPDATE_NOT_SUBMITTED");

            Exception exception = assertThrows(BadRequestException.class, () ->
                hearingManagementService.deleteHearingRequest(hearingId, deleteHearingRequest));
            assertEquals(INVALID_DELETE_HEARING_STATUS, exception.getMessage());
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getVersionNumber(hearingId);
        }

        @Test
        void testExpectedException_DeleteHearing_VersionNumber_Not_Equal_To_DB_VersionNumber() {
            final long hearingId = 2000000000L;
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(2);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(hearingId, deleteHearingRequest));
            assertEquals(INVALID_VERSION_NUMBER, exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_NotPresent_inDB() {
            final long hearingId = 2000000000L;
            when(hearingRepository.existsById(hearingId)).thenReturn(false);
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
                .deleteHearingRequest(hearingId, deleteHearingRequest));
            assertEquals("No hearing found for reference: " + hearingId, exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_Null() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(null, deleteHearingRequest));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_Exceeds_MaxLength() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(20000000001111L, deleteHearingRequest));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_First_Char_Is_Not_2() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(1000000100L, deleteHearingRequest));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void deleteHearingShouldIncrementVersionNumber() {
            final long hearingId = 2000000000L;
            DeleteHearingRequest hearingRequest = TestingUtil.deleteHearingRequest();
            final int versionNumber = hearingRequest.getVersionNumber();
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.getStatus(hearingId)).thenReturn(DeleteHearingStatus.UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(
                hearingId,
                DeleteHearingStatus.HEARING_REQUESTED.name(),
                1
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);

            HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
                hearingId, hearingRequest);
            // Check that version number has been incremented
            assertEquals((versionNumber + 1), hearingResponse.getVersionNumber());
        }
    }

    @Nested
    @DisplayName("updateHearing")
    class UpdateHearing {
        @Test
        void updateHearingRequestShouldPassWithValidDetails() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.UPDATE_REQUESTED.name(),
                                                                versionNumber
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenVersionNumberDoesNotMatchRequest() {
            final long hearingId = 2000000000L;
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(6);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(hearingId, updateHearingRequest));
            assertEquals(INVALID_VERSION_NUMBER, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenDbStatusDoesNotMatchWithExpectedState() {
            final long hearingId = 2000000000L;
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(1);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("HEARING_NOT_REQUESTED");
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(hearingId, updateHearingRequest));
            assertEquals(INVALID_PUT_HEARING_STATUS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldPassWhenDbStatusMatchWithExpectedState() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(
                hearingRequest.getRequestDetails().getVersionNumber());
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.UPDATE_REQUESTED.name());

            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.UPDATE_REQUESTED.name(),
                                                                hearingRequest.getRequestDetails().getVersionNumber()
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
            assertEquals(hearingResponse.getHearingRequestId(), hearingId);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingRequestDetailsNull() {
            UpdateHearingRequest request = new UpdateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingWindowFieldsAreNull() {
            UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingDetails.setHearingWindow(hearingWindow);
            request.setHearingDetails(hearingDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_HEARING_WINDOW, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsNull() {
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
            hearingDetails.setHearingWindow(hearingWindow);
            PartyDetails partyDetails = new PartyDetails();
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsBothExist() {
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
            hearingDetails.setHearingWindow(hearingWindow);
            PartyDetails partyDetails = new PartyDetails();
            OrganisationDetails organisationDetails = new OrganisationDetails();
            partyDetails.setOrganisationDetails(organisationDetails);
            IndividualDetails individualDetails = new IndividualDetails();
            partyDetails.setIndividualDetails(individualDetails);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyUnavailabilityDowIsNotPresent() {
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
            hearingDetails.setHearingWindow(hearingWindow);
            PartyDetails partyDetails = new PartyDetails();
            IndividualDetails individualDetails = new IndividualDetails();
            individualDetails.setHearingChannelEmail("email");
            partyDetails.setIndividualDetails(individualDetails);
            List<UnavailabilityDow> unavailabilityDowList = new ArrayList<>();
            partyDetails.setUnavailabilityDow(unavailabilityDowList);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_UNAVAILABILITY_DOW_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyUnavailabilityRangesIsNotPresent() {
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
            hearingDetails.setHearingWindow(hearingWindow);
            PartyDetails partyDetails = new PartyDetails();
            IndividualDetails individualDetails = new IndividualDetails();
            individualDetails.setHearingChannelEmail("email");
            partyDetails.setIndividualDetails(individualDetails);
            List<UnavailabilityRanges> unavailabilityRanges = new ArrayList<>();
            partyDetails.setUnavailabilityRanges(unavailabilityRanges);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_UNAVAILABILITY_RANGES_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenRelatedPartyDetailsAreNotPresent() {
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            HearingWindow hearingWindow = new HearingWindow();
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
            hearingDetails.setHearingWindow(hearingWindow);
            PartyDetails partyDetails = new PartyDetails();
            IndividualDetails individualDetails = new IndividualDetails();
            individualDetails.setHearingChannelEmail("email");
            List<RelatedParty> relatedParties = new ArrayList<>();
            individualDetails.setRelatedParties(relatedParties);
            partyDetails.setIndividualDetails(individualDetails);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request));
            assertEquals(INVALID_RELATED_PARTY_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdIsNull() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(null, updateHearingRequest));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdExceedsMaxLength() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(20000000001111L, updateHearingRequest));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdDoesNotStartWith2() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(1000000100L, updateHearingRequest));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdNotPresentInDB() {
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, updateHearingRequest));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void updateHearingShouldIncrementVersionNumberBy1() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(caseHearingRequestRepository.getVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.HEARING_REQUESTED.name());

            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.HEARING_REQUESTED.name(),
                                                                versionNumber
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.save(hearingEntity)).thenReturn(hearingEntity);

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
            // Check that version number has been incremented
            assertEquals((versionNumber + 1), hearingResponse.getVersionNumber());
        }
    }

    @Nested
    @DisplayName("getHearings")
    class GetHearings {

        @Test
        void getHearings_shouldReturnDataWithValidDetails() {
            List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
            when(caseHearingRequestRepository.getHearingDetailsWithStatus("12345", "HEARING_REQUESTED"))
                .thenReturn(entities);
            given(getHearingsResponseMapper.toHearingsResponse("12345", entities))
                .willReturn(TestingUtil.getHearingsResponseWhenDataIsPresent("12345"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345", "HEARING_REQUESTED");
            assertEquals("12345", response.getCaseRef());
            assertEquals("AB1A", response.getHmctsServiceId());
            assertEquals(1, response.getCaseHearings().size());
            assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
            assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
            assertEquals("venue", response.getCaseHearings().get(0)
                .getHearingDaySchedule().get(0).getHearingVenueId());
            assertEquals("subChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
                .getAttendees().get(0).getHearingSubChannel());
            assertEquals("judge1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
                .getHearingJudgeId());
        }

        @Test
        void getHearings_shouldReturnNoDataWithStatus_Null() {
            when(caseHearingRequestRepository.getHearingDetails("12345")).thenReturn(null);
            given(getHearingsResponseMapper.toHearingsResponse("12345", null))
                .willReturn(TestingUtil.getHearingsResponseWhenNoData("12345"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345", null);
            assertEquals("12345", response.getCaseRef());
            assertNull(response.getHmctsServiceId());
            assertEquals(0, response.getCaseHearings().size());
        }

        @Test
        void getHearings_shouldReturnNoDataWithInValidStatus() {
            when(caseHearingRequestRepository.getHearingDetailsWithStatus("12345", "InvalidStatus"))
                .thenReturn(null);
            given(getHearingsResponseMapper.toHearingsResponse("12345", null))
                .willReturn(TestingUtil.getHearingsResponseWhenNoData("12345"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345", "InvalidStatus");
            assertEquals("12345", response.getCaseRef());
            assertNull(response.getHmctsServiceId());
            assertEquals(0, response.getCaseHearings().size());
        }
    }

    @Nested
    @DisplayName("sendRequestToHmiAndQueue")
    class SendRequestToHmiAndQueue {

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenCreateRequestHasPartyDetails() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(
                1L,
                createHearingRequest
            )).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, createHearingRequest, REQUEST_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, createHearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenCreateRequestHasOnlyMandatoryFields() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(
                1L,
                createHearingRequest
            )).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, createHearingRequest, REQUEST_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(
                1L,
                createHearingRequest
            );
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenCreateRequestHasNoOrgsDetailsPresent() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            createHearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(
                1L,
                createHearingRequest
            )).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, createHearingRequest, REQUEST_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1))
                .mapRequest(1L, createHearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenCreateRequestHasNoIndividualDetailsPresent() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            createHearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(
                1L,
                createHearingRequest
            )).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, createHearingRequest, REQUEST_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(
                1L,
                createHearingRequest
            );
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenCreateRequestHasNoRelatedPartyDetailsPresent() {
            CreateHearingRequest createHearingRequest = new CreateHearingRequest();
            createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
            createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
            createHearingRequest.setPartyDetails(TestingUtil.partyDetails());
            createHearingRequest.getPartyDetails().get(0)
                .setIndividualDetails(TestingUtil.individualWithoutRelatedPartyDetails());
            createHearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(
                1L,
                createHearingRequest
            )).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, createHearingRequest, REQUEST_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(
                1L,
                createHearingRequest
            );
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenUpdateRequestHasPartyDetails() {
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(1L, hearingRequest)).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, hearingRequest, AMEND_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, hearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenUpdateRequestHasOnlyMandatoryFields() {
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(1L, hearingRequest)).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, hearingRequest, AMEND_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, hearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenUpdateRequestHasNoOrgsDetailsPresent() {
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(1L, hearingRequest)).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, hearingRequest, AMEND_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, hearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenUpdateRequestHasNoIndividualDetailsPresent() {
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(1L, hearingRequest)).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, hearingRequest, AMEND_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, hearingRequest);
        }

        @Test
        void shouldSuccessfullyMapToHmiFormatWhenUpdateRequestHasNoRelatedPartyDetailsPresent() {
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil
                                                                             .individualWithoutRelatedPartyDetails());
            HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
            when(hmiSubmitHearingRequestMapper.mapRequest(1L, hearingRequest)).thenReturn(hmiSubmitHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
            doNothing().when(messageSenderToQueueConfiguration).sendMessageToQueue(any(), anyLong(), anyString());
            hearingManagementService.sendRequestToHmiAndQueue(1L, hearingRequest, AMEND_HEARING);
            verify(hmiSubmitHearingRequestMapper, times(1)).mapRequest(1L, hearingRequest);
        }

    }

    /**
     * generate Hearing Entity.
     *
     * @param hearingId Hearing Id
     * @param status    status
     * @return hearingEntity Hearing Entity
     */
    private HearingEntity generateHearingEntity(Long hearingId, String status, Integer versionNumber) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setVersionNumber(versionNumber);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private HmiSubmitHearingRequest getHmiSubmitHearingRequest() {
        HmiCaseDetails hmiCaseDetails = HmiCaseDetails.builder().build();
        Listing listing = Listing.builder().build();
        Entity entity = Entity.builder().build();
        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetails)
            .listing(listing)
            .entities(Collections.singletonList(entity))
            .build();
        HmiSubmitHearingRequest hmiSubmitHearingRequest = HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
        return hmiSubmitHearingRequest;
    }
}
