package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.datastore.model.CaseSearchResult;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToQueueConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.PartyRelationshipDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.helper.GetHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.GetHearingsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.PartyRelationshipDetailsMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.EntitiesMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.EntitiesMapperObject;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiCaseDetailsMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiDeleteHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiSubmitHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.helper.hmi.ListingMapper;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingActualsValidator;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_DETAILS_ARE_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WINDOW_EMPTY_NULL;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.COMPLETED;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.INVALID_CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingPartyEntityInd;

@ExtendWith(MockitoExtension.class)
class HearingManagementServiceTest {

    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String USER_ID = "UserId";
    public static final String ROLE_NAME = "hearing-manager";
    public static final String ROLE_TYPE = "ORGANISATION";
    private static final String CLIENT_S2S_TOKEN = "s2s_token";

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
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    LinkedHearingDetailsRepository linkedHearingDetailsRepository;

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
    MessageSenderToQueueConfiguration messageSenderToQueueConfiguration;

    @Mock
    ActualHearingRepository actualHearingRepository;

    @Mock
    ActualHearingDayRepository  actualHearingDayRepository;

    AccessControlServiceImpl accessControlService;

    @Mock
    ApplicationParams applicationParams;

    @Mock
    PartyRelationshipDetailsMapper partyRelationshipDetailsMapper;

    HearingIdValidator hearingIdValidator;

    LinkedHearingValidator linkedHearingValidator;

    HearingActualsValidator hearingActualsValidator;

    @Mock
    ListingMapper listingMapper;

    @Mock
    HmiCaseDetailsMapper hmiCaseDetailsMapper;

    @Mock
    EntitiesMapper entitiesMapper;

    @Mock
    HmiHearingResponseMapper hmiHearingResponseMapper;

    JsonNode jsonNode = mock(JsonNode.class);

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    PendingRequestService pendingRequestService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                actualHearingDayRepository);
        linkedHearingValidator = new LinkedHearingValidator(hearingIdValidator, hearingRepository,
                      linkedGroupDetailsRepository, linkedHearingDetailsRepository);
        hearingActualsValidator = new HearingActualsValidator(hearingIdValidator);
        accessControlService = new AccessControlServiceImpl(roleAssignmentService,
                                                            securityUtils,
                                                            dataStoreRepository,
                                                            caseHearingRequestRepository,
                                                            hearingRepository,
                                                            applicationParams);
        hearingManagementService = createHearingManagementService();

        hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(any(),any(),any(),any(),any(),any(),any());
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(any(),any(),any(),any(),any(),any(),any());
    }


    @Nested
    @DisplayName("SendResponseToTopic")
    class SendResponseToTopic {
        @Test
        void shouldVerifySubsequentCalls() throws JsonProcessingException {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            JsonNode jsonNode1 = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            when(objectMapperService.convertObjectToJsonNode(json)).thenReturn(jsonNode1);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(Mockito.any(), any(),any(), any());
            hearingManagementService.sendResponse(json, "test hmctsCode", null);
            verify(objectMapperService, times(1)).convertObjectToJsonNode(any());
            verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any(), any(),any(),
                                                                            any());
        }

        @Test
        void shouldVerifySubsequentCallsWhenDeploymentIdIsPresent() throws JsonProcessingException {
            ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            JsonNode jsonNode1 = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            when(objectMapperService.convertObjectToJsonNode(json)).thenReturn(jsonNode1);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(Mockito.any(), any(),any(), any());
            hearingManagementService.sendResponse(json, "test hmctsCode", "TEST");
            verify(objectMapperService, times(1)).convertObjectToJsonNode(any());
            verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any(), any(),any(),
                                                                            any());
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
            HearingEntity hearing = TestingUtil.hearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearing));
            hearingManagementService.getHearingRequest(2000000000L, true);
            verify(hearingRepository).existsById(2000000000L);
        }

        @Test
        void shouldPassWithValidHearingIdInDb() {
            HearingEntity hearing = TestingUtil.hearingEntity();
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
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(null);
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(null);
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(HEARING_WINDOW_EMPTY_NULL, exception.getMessage());
        }

        @Test
        void shouldFailIfHearingWindowDetailsHasUnexpectedFieldsInGroup_firstScenario() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(LocalDate.parse("2017-03-01"));
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(LocalDate.parse("2017-03-01"));
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(LocalDateTime.now());
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(HEARING_WINDOW_DETAILS_ARE_INVALID, exception.getMessage());
        }

        @Test
        void shouldFailIfHearingWindowDetailsHasUnexpectedFieldsInGroup_secondScenario() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(LocalDate.parse("2017-03-01"));
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(null);
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(LocalDateTime.now());
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(HEARING_WINDOW_DETAILS_ARE_INVALID, exception.getMessage());
        }

        @Test
        void shouldFailIfHearingWindowDetailsHasUnexpectedFieldsInGroup_thirdScenario() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(null);
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(LocalDate.parse("2017-03-01"));
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(LocalDateTime.now());
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(HEARING_WINDOW_DETAILS_ARE_INVALID, exception.getMessage());
        }

        @Test
        void shouldPassIfHearingDetailsHasExpectedField_firstDateTimeMustBe() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeStart(null);
            hearingRequest.getHearingDetails().getHearingWindow().setDateRangeEnd(null);
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(LocalDateTime.now());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService. saveHearingRequest(hearingRequest, null,
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldPassIfHearingDetailsHasExpectedFields() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);

            assertNotNull(hearingRequest.getHearingDetails().getHearingWindow().getDateRangeStart());
            assertNotNull(hearingRequest.getHearingDetails().getHearingWindow().getDateRangeEnd());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(), anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, null,
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldFailIfNullHearingRequest() {
            HearingRequest hearingRequest = null;
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest,  null, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailIfChannelTypeIsNotUnique() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());

            assertEquals(2,hearingRequest.getHearingDetails().getHearingChannels().size());
            hearingRequest.getHearingDetails().setHearingChannels(List.of("sameChannelType","sameChannelType"));

            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                 .saveHearingRequest(hearingRequest,  null, CLIENT_S2S_TOKEN));
            assertEquals(ValidationError.NON_UNIQUE_CHANNEL_TYPE, exception.getMessage());
        }

        @Test
        void shouldFailAsDetailsNotPresent() {
            HearingRequest hearingRequest = new HearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .saveHearingRequest(hearingRequest,  null, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailIfNoCorrespondingHearingPartyTechIdInDatabase() {
            HearingDetails hearingDetails = buildHearingDetails();
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(hearingDetails);
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());

            final HearingEntity hearingEntity = mock(HearingEntity.class);
            CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
            caseHearingRequestEntity.setHearingParties(List.of(new HearingPartyEntity()));
            when(hearingEntity.getLatestCaseHearingRequest()).thenReturn(caseHearingRequestEntity);

            given(hearingRepository.save(any())).willReturn(hearingEntity);
            given(partyRelationshipDetailsMapper.modelToEntity(any(), any()))
                    .willThrow(BadRequestException.class);
            mockGetEntities(hearingRequest);

            assertThrows(BadRequestException.class, () -> hearingManagementService
                   .saveHearingRequest(hearingRequest, null,CLIENT_S2S_TOKEN));
        }

        @Test
        void shouldPassWithHearing_Case_Request_Details_Valid() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, null,
                CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldPassWithHearing_Case_Request_Party_Details_Valid() {
            HearingDetails hearingDetails = buildHearingDetails();
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(hearingDetails);
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());

            PartyRelationshipDetailsEntity entity1 = PartyRelationshipDetailsEntity.builder()
                    .partyRelationshipDetailsId(1L)
                    .relationshipType("type1")
                    .sourceTechParty(hearingPartyEntityInd())
                    .targetTechParty(hearingPartyEntityInd())
                    .build();

            PartyRelationshipDetailsEntity entity2 = PartyRelationshipDetailsEntity.builder()
                    .partyRelationshipDetailsId(2L)
                    .relationshipType("type2")
                    .sourceTechParty(hearingPartyEntityInd())
                    .targetTechParty(hearingPartyEntityInd())
                    .build();

            final List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntities = List.of(entity1, entity2);
            given(partyRelationshipDetailsMapper.modelToEntity(any(), any()))
                    .willReturn(partyRelationshipDetailsEntities);
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, null,
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldPassWithRelatedParty_Details_Present() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(
                TestingUtil.individualWithoutRelatedPartyDetails());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, null,
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldPassWithRelatedParty_Details_Not_Present() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.setPartyDetails(TestingUtil.partyDetails());
            hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
            hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),anyBoolean(), anyBoolean(),
                                              any()))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, null,
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldPassIfHearingDeploymentIdIsPresent() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);

            assertNotNull(hearingRequest.getHearingDetails().getHearingWindow().getDateRangeStart());
            assertNotNull(hearingRequest.getHearingDetails().getHearingWindow().getDateRangeEnd());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();
            given(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(), anyBoolean(), anyBoolean(),
                                              eq("TEST")))
                .willReturn(TestingUtil.hearingEntity());
            given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
            HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest, "TEST",
                                                                                   CLIENT_S2S_TOKEN);
            assertValidHearingResponse(response);
        }

        @Test
        void shouldFailWithParty_Details_InValid_Org_Individual_details_Present() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            List<PartyDetails> partyDetails = TestingUtil.partyDetails();
            partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            hearingRequest.setPartyDetails(partyDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                 .saveHearingRequest(hearingRequest,  null, CLIENT_S2S_TOKEN));
            assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
        }

        private void assertValidHearingResponse(HearingResponse response) {
            assertEquals(VERSION_NUMBER_TO_INCREMENT, response.getVersionNumber());
            assertEquals(POST_HEARING_STATUS, response.getStatus());
            assertNotNull(response.getHearingRequestId());
        }
    }

    @Nested
    @DisplayName("VerifyAccess")
    class VerifyAccess {

        @BeforeEach
        void setUp() {
            doReturn(true).when(applicationParams).isAccessControlEnabled();
        }

        private void stubRoleAssignments(RoleAssignmentAttributes.RoleAssignmentAttributesBuilder builder,
                                         String hearingManager) {
            RoleAssignmentAttributes roleAssignmentAttributes = builder
                .build();
            RoleAssignment roleAssignment = RoleAssignment.builder()
                .roleName(hearingManager)
                .roleType(ROLE_TYPE)
                .attributes(roleAssignmentAttributes)
                .build();
            List<RoleAssignment> roleAssignmentList = new ArrayList<>();
            roleAssignmentList.add(roleAssignment);
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(roleAssignmentList)
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesCaseJurisdictionAndCaseTypeId() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), HEARING_MANAGER);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(HEARING_MANAGER), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesOnlyCaseJurisdiction() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId("different casetypeid")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldRejectAccessWhenRoleAssignmentValidAndMatchesOnlyCaseTypeId() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), HEARING_VIEWER);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("different Jurisdiction")
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(HEARING_VIEWER);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles, null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndNoJurisdictionOrCaseTypeIdGivenInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder(), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionAndCaseTypeIdIsEmptyInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.empty())
                                    .caseType(Optional.empty()), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("different Jurisdiction")
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsEmptyInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.empty())
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsEmptyInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.empty()), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsNullInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsNullInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsEmptyAndJurisdictionIsNullInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .caseType(Optional.empty()), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsNullAndJurisdictionIsEmptyInRoleAssignment() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.empty()), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            accessControlService.verifyCaseAccess(CASE_REFERENCE, Lists.newArrayList(ROLE_NAME), null);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenNoRoleAssignmentsReturned() {
            doReturn(USER_ID).when(securityUtils).getUserId();
            RoleAssignments roleAssignments = RoleAssignments.builder()
                .roleAssignments(Lists.newArrayList())
                .build();
            doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
            List<String> requiredRoles = Lists.newArrayList();
            Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles, null));
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
            List<String> requiredRoles = Lists.newArrayList();
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles,null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ROLE, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenRoleAssignmentDoesNotMatchCaseDetails() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(ROLE_NAME);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles,null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsNull() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(ROLE_NAME);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles, null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsDifferent() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(ROLE_NAME);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles, null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsDifferent() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), ROLE_NAME);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(ROLE_NAME);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(CASE_REFERENCE, requiredRoles, null));
            assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
        }

        @Test
        void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsEmpty() {
            stubRoleAssignments(RoleAssignmentAttributes.builder()
                                    .jurisdiction(Optional.of(JURISDICTION))
                                    .caseType(Optional.of(CASE_TYPE)), HEARING_MANAGER);
            doReturn(USER_ID).when(securityUtils).getUserId();
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .jurisdiction("Different Jurisdiction")
                .caseTypeId("Different CaseTypeId")
                .build();
            doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(INVALID_CASE_REFERENCE);
            List<String> requiredRoles = Lists.newArrayList(ROLE_NAME);
            Exception exception = assertThrows(InvalidRoleAssignmentException.class, () ->
                accessControlService.verifyCaseAccess(INVALID_CASE_REFERENCE, requiredRoles, null));
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
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("UPDATE_SUBMITTED");
            HearingEntity hearingEntity = generateHearingEntity(
                hearingId,
                CANCELLATION_REQUESTED,
                1
            );
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(deleteHearingRequest), any(), any(), any())).thenReturn(hearingEntity);
            mockDeleteRequest();

            HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
                hearingId, TestingUtil.deleteHearingRequest(), CLIENT_S2S_TOKEN);
            assertEquals(versionNumber + 1, hearingResponse.getVersionNumber());
            assertEquals(CANCELLATION_REQUESTED, hearingResponse.getStatus());
            assertNotNull(hearingResponse.getHearingRequestId());
            verify(hearingRepository).existsById(hearingId);
        }

        @Test
        void deleteHearingRequestShouldFailWithInvalidStatus() {
            final long hearingId = 2000000000L;
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("UPDATE_NOT_SUBMITTED");

            Exception exception = assertThrows(BadRequestException.class, () ->
                hearingManagementService.deleteHearingRequest(hearingId, deleteHearingRequest,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_DELETE_HEARING_STATUS, exception.getMessage());
            verify(hearingRepository).existsById(hearingId);
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_NotPresent_inDB() {
            final long hearingId = 2000000000L;
            when(hearingRepository.existsById(hearingId)).thenReturn(false);
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
                .deleteHearingRequest(hearingId, deleteHearingRequest,CLIENT_S2S_TOKEN));
            assertEquals("No hearing found for reference: " + hearingId, exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_Null() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(null, deleteHearingRequest,CLIENT_S2S_TOKEN));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_Exceeds_MaxLength() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(20000000001111L, deleteHearingRequest,CLIENT_S2S_TOKEN));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void testExpectedException_DeleteHearing_HearingId_First_Char_Is_Not_2() {
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .deleteHearingRequest(1000000100L, deleteHearingRequest,CLIENT_S2S_TOKEN));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }

        @Test
        void deleteHearingShouldIncrementVersionNumber() {
            final long hearingId = 2000000000L;
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(DeleteHearingStatus.UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(
                hearingId,
                DeleteHearingStatus.HEARING_REQUESTED.name(),
                1
            );
            DeleteHearingRequest deleteHearingRequest = TestingUtil.deleteHearingRequest();
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(deleteHearingRequest), any(), any(), any())).thenReturn(hearingEntity);

            DeleteHearingRequest hearingRequest = TestingUtil.deleteHearingRequest();
            mockDeleteRequest();

            HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
                hearingId, hearingRequest,CLIENT_S2S_TOKEN);
            // Check that version number has been incremented
            assertNotNull(hearingResponse.getVersionNumber());
        }

        private void mockDeleteRequest() {
            HearingRequest hearingRequest = new HearingRequest();
            hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
            hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
            hearingRequest.setCaseDetails(TestingUtil.caseDetails());
            hearingRequest.setPartyDetails(TestingUtil.partyDetailsWithOrgType());
            hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
            HmiDeleteHearingRequest hmiDeleteHearingRequest = getHmiDeleteHearingRequest();
            when(hmiDeleteHearingRequestMapper.mapRequest()).thenReturn(hmiDeleteHearingRequest);
            when(objectMapperService.convertObjectToJsonNode(hmiDeleteHearingRequest)).thenReturn(jsonNode);
        }
    }

    @Nested
    @DisplayName("updateHearing")
    class UpdateHearing {
        @BeforeEach
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                    actualHearingDayRepository);
            linkedHearingValidator = new LinkedHearingValidator(hearingIdValidator, hearingRepository,
                    linkedGroupDetailsRepository, linkedHearingDetailsRepository);
            hearingActualsValidator = new HearingActualsValidator(hearingIdValidator);
            accessControlService = new AccessControlServiceImpl(roleAssignmentService,
                    securityUtils,
                    dataStoreRepository,
                    caseHearingRequestRepository,
                    hearingRepository,
                    applicationParams);
            hearingManagementService = createHearingManagementService();
        }

        @Test
        void updateHearingRequestShouldPassWithValidDetails() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            mockValidHearing(hearingId, versionNumber, hearingRequest);
            mockGetEntities(hearingRequest);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            null,CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldPassWithValidPlannedResponses() {
            val hearingDayDetailsEntity = new HearingDayDetailsEntity();
            hearingDayDetailsEntity.setStartDateTime(LocalDateTime.now());

            val hearingResponseEntity = new HearingResponseEntity();
            hearingResponseEntity.setRequestTimeStamp(LocalDateTime.now());
            hearingResponseEntity.setRequestVersion(2);

            hearingDayDetailsEntity.setEndDateTime(LocalDateTime.now());
            hearingResponseEntity.setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity));
            val hearingRequest = TestingUtil.updateHearingRequest();
            val versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            val hearingId = 2000000000L;
            val hearingEntity = generateHearingEntity(
                hearingId,
                UPDATE_REQUESTED.name(),
                versionNumber
            );
            hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity));

            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),
                                             anyBoolean(), anyBoolean(), any())).thenReturn(hearingEntity);
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());
            mockSubmitRequest();
            mockGetEntities(hearingRequest);

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            null,
                                                                                            CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldPassWithOutValidPlannedResponses() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            HearingEntity hearingEntity = generateHearingEntity(hearingId, UPDATE_REQUESTED.name(),
                                                                versionNumber
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),
                                             anyBoolean(), anyBoolean(), any())).thenReturn(hearingEntity);
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());
            mockGetEntities(hearingRequest);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            null,
                                                                                            CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldThrowErrorDueToPlannedResponse() {
            val hearingDayDetailsEntity = new HearingDayDetailsEntity();
            hearingDayDetailsEntity.setStartDateTime(
                LocalDateTime.of(2021, 5, 20,10,10,10)
            );

            val hearingResponseEntity = new HearingResponseEntity();
            val plannedResponse = LocalDateTime.of(2021, 5, 20,10,10,10);
            hearingResponseEntity.setRequestTimeStamp(plannedResponse);
            hearingResponseEntity.setRequestVersion(2);
            hearingDayDetailsEntity.setEndDateTime(LocalDateTime.now());
            hearingResponseEntity.setHearingDayDetails(Arrays.asList(hearingDayDetailsEntity));

            val hearingRequest = TestingUtil.updateHearingRequest();
            val hearingId = 2000000000L;
            val versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            val hearingEntity = generateHearingEntity(hearingId,
                                                      UPDATE_REQUESTED.name(),
                                                      versionNumber
            );
            hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity));

            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());

            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(hearingId, hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_PUT_HEARING_STATUS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenVersionNumberDoesNotMatchRequest() {
            final long hearingId = 2000000000L;
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(6);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(hearingId, updateHearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_VERSION_NUMBER, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenDbStatusDoesNotMatchWithExpectedState() {
            final long hearingId = 2000000000L;
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(1);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn("HEARING_NOT_REQUESTED");
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(hearingId, updateHearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_PUT_HEARING_STATUS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldPassWhenDbStatusMatchWithExpectedState() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(
                hearingRequest.getRequestDetails().getVersionNumber());
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, UPDATE_REQUESTED.name(),
                                                                hearingRequest.getRequestDetails().getVersionNumber()
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),
                                             anyBoolean(), anyBoolean(), any())).thenReturn(hearingEntity);
            mockGetEntities(hearingRequest);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            null,
                                                                                            CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getHearingRequestId(), hearingId);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingRequestDetailsNull() {
            UpdateHearingRequest request = new UpdateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingWindowFieldsAreNull() {
            final UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = new HearingDetails();
            hearingDetails.setAutoListFlag(true);
            hearingDetails.setAmendReasonCodes(List.of("reason"));
            HearingWindow hearingWindow = new HearingWindow();
            hearingDetails.setHearingWindow(hearingWindow);
            request.setHearingDetails(hearingDetails);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request, null,CLIENT_S2S_TOKEN));
            assertEquals(HEARING_WINDOW_EMPTY_NULL, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsNull() {
            HearingDetails hearingDetails = buildHearingDetails();
            PartyDetails partyDetails = new PartyDetails();
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsBothExist() {
            PartyDetails partyDetails = new PartyDetails();
            OrganisationDetails organisationDetails = new OrganisationDetails();
            partyDetails.setOrganisationDetails(organisationDetails);
            IndividualDetails individualDetails = new IndividualDetails();
            partyDetails.setIndividualDetails(individualDetails);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = buildHearingDetails();
            request.setHearingDetails(hearingDetails);
            request.setPartyDetails(partyDetailsList);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, request, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldNotErrorWhenPartyUnavailabilityDowIsNotPresent() {
            PartyDetails partyDetails = TestingUtil.partyDetails().get(0);
            partyDetails.setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.getIndividualDetails().setHearingChannelEmail(List.of("email"));
            List<UnavailabilityDow> unavailabilityDowList = new ArrayList<>();
            partyDetails.setUnavailabilityDow(unavailabilityDowList);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            RequestDetails requestDetails = new RequestDetails();
            requestDetails.setVersionNumber(1);
            UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = buildHearingDetails();
            request.setHearingDetails(hearingDetails);
            request.setCaseDetails(TestingUtil.caseDetails());
            request.setPartyDetails(partyDetailsList);
            request.setRequestDetails(requestDetails);

            final long hearingId = 2000000000L;
            final int versionNumber = request.getRequestDetails().getVersionNumber();
            mockValidHearing(hearingId, versionNumber, request);
            mockGetEntities(request);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, request,
                                                                                            null,
                                                                                            CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldNotErrorWhenPartyUnavailabilityRangesIsNotPresent() {
            PartyDetails partyDetails = TestingUtil.partyDetails().get(0);
            partyDetails.setIndividualDetails(TestingUtil.individualDetails());
            partyDetails.getIndividualDetails().setHearingChannelEmail(List.of("email"));
            List<UnavailabilityRanges> unavailabilityRanges = new ArrayList<>();
            partyDetails.setUnavailabilityRanges(unavailabilityRanges);
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            RequestDetails requestDetails = new RequestDetails();
            requestDetails.setVersionNumber(1);
            UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = buildHearingDetails();
            request.setHearingDetails(hearingDetails);
            request.setCaseDetails(TestingUtil.caseDetails());
            request.setPartyDetails(partyDetailsList);
            request.setRequestDetails(requestDetails);

            final long hearingId = 2000000000L;
            final int versionNumber = request.getRequestDetails().getVersionNumber();
            mockValidHearing(hearingId, versionNumber, request);
            mockGetEntities(request);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, request,
                                                                                            null,
                                                                                            CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldNotErrorWhenRelatedPartyDetailsAreNotPresent() {
            PartyDetails partyDetails = TestingUtil.partyDetails().get(0);
            IndividualDetails individualDetails = TestingUtil.individualDetails();
            individualDetails.setHearingChannelEmail(List.of("email"));
            List<RelatedParty> relatedParties = new ArrayList<>();
            individualDetails.setRelatedParties(relatedParties);
            partyDetails.setIndividualDetails(individualDetails);
            partyDetails.getIndividualDetails().setRelatedParties(new ArrayList<>());
            List<PartyDetails> partyDetailsList = new ArrayList<>();
            partyDetailsList.add(partyDetails);
            RequestDetails requestDetails = new RequestDetails();
            requestDetails.setVersionNumber(1);
            UpdateHearingRequest request = new UpdateHearingRequest();
            HearingDetails hearingDetails = buildHearingDetails();
            request.setHearingDetails(hearingDetails);
            request.setCaseDetails(TestingUtil.caseDetails());
            request.setPartyDetails(partyDetailsList);
            request.setRequestDetails(requestDetails);

            final long hearingId = 2000000000L;
            final int versionNumber = request.getRequestDetails().getVersionNumber();
            mockGetEntities(request);
            mockValidHearing(hearingId, versionNumber, request);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, request,
                                                                                            null,CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdIsNull() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(null, updateHearingRequest,null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdExceedsMaxLength() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(20000000001111L, updateHearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdDoesNotStartWith2() {
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(1000000100L, updateHearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldThrowErrorWhenHearingIdNotPresentInDB() {
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);
            UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
            Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, updateHearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldPassWithCaseRefSameAsInPost() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            mockValidHearing(hearingId, versionNumber, hearingRequest);
            mockGetEntities(hearingRequest);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            null,CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        @Test
        void updateHearingRequestShouldFailAsCaseRefIsChangedForUpdate() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            hearingRequest.getCaseDetails().setCaseRef("1111222233334455");
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, UPDATE_REQUESTED.name(),
                                                                versionNumber
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .updateHearingRequest(2000000000L, hearingRequest, null,CLIENT_S2S_TOKEN));
            assertEquals(ValidationError.INVALID_CASE_REFERENCE, exception.getMessage());
        }

        @Test
        void updateHearingRequestShouldPassWithDeploymentId() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            mockValidHearing(hearingId, versionNumber, hearingRequest);
            mockGetEntities(hearingRequest);
            mockSubmitRequest();

            HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest,
                                                                                            "ABA",CLIENT_S2S_TOKEN);
            assertEquals(hearingResponse.getVersionNumber(), versionNumber + 1);
            verify(hearingRepository).existsById(hearingId);
            verify(caseHearingRequestRepository).getLatestVersionNumber(hearingId);
        }

        private int mockValidHearing(long hearingId, int versionNumber, UpdateHearingRequest hearingRequest) {

            when(caseHearingRequestRepository.getLatestVersionNumber(hearingId)).thenReturn(versionNumber);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.getStatus(hearingId)).thenReturn(UPDATE_REQUESTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, UPDATE_REQUESTED.name(),
                                                                versionNumber
            );
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingMapper.modelToEntity(eq(hearingRequest), any(), any(), any(),
                                             anyBoolean(), anyBoolean(), any())).thenReturn(hearingEntity);
            return versionNumber;
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
                .willReturn(TestingUtil.getHearingsResponseWhenDataIsPresent("12345", "HEARING_REQUESTED"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345",
                    "HEARING_REQUESTED");
            assertEquals("12345", response.getCaseRef());
            assertEquals("AB1A", response.getHmctsServiceCode());
            assertEquals(1, response.getCaseHearings().size());
            assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
            assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
            assertEquals("venue", response.getCaseHearings().get(0)
                .getHearingDaySchedule().get(0).getHearingVenueId());
            assertEquals("subChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
                .getAttendees().get(0).getHearingSubChannel());
            assertEquals("judge1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
                .getHearingJudgeId());
            assertEquals("someChannelType", response.getCaseHearings().get(0).getHearingChannels().get(0));
        }

        @Test
        void getHearings_shouldReturnNoDataWithStatus_Null() {
            when(caseHearingRequestRepository.getHearingDetails("12345")).thenReturn(null);
            given(getHearingsResponseMapper.toHearingsResponse("12345", null))
                .willReturn(TestingUtil.getHearingsResponseWhenNoData("12345"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345", null);
            assertEquals("12345", response.getCaseRef());
            assertNull(response.getHmctsServiceCode());
            assertEquals(0, response.getCaseHearings().size());
        }

        @Test
        void getHearings_shouldReturnNoDataWithInValidStatus() {
            when(caseHearingRequestRepository.getHearingDetailsWithStatus("12345", "InvalidStatus"))
                .thenReturn(null);
            given(getHearingsResponseMapper.toHearingsResponse("12345", null))
                .willReturn(TestingUtil.getHearingsResponseWhenNoData("12345"));
            GetHearingsResponse response = hearingManagementService.getHearings("12345",
                    "InvalidStatus");
            assertEquals("12345", response.getCaseRef());
            assertNull(response.getHmctsServiceCode());
            assertEquals(0, response.getCaseHearings().size());
        }
    }

    @Nested
    @DisplayName("hearingCompletion")
    class HearingCompletion {

        @Test
        void shouldThrowExceptionWhenHearingIdIsNull() {
            Exception exception = assertThrows(BadRequestException.class,
                                               () -> hearingManagementService.hearingCompletion(null,
                                                                                                CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenHearingIdNotFound() {
            final long hearingId = 2000000000L;
            TestingUtil.updateHearingRequest();
            when(hearingRepository.existsById(hearingId)).thenReturn(false);
            Exception exception = assertThrows(HearingNotFoundException.class,
                                               () -> hearingManagementService.hearingCompletion(hearingId,
                                                                                                CLIENT_S2S_TOKEN));
            assertEquals("001 No such id: 2000000000", exception.getMessage());
            verify(hearingRepository).existsById(hearingId);
        }

        @Test
        void shouldThrowErrorWhenHearingIdIsNotValidFormat() {
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .hearingCompletion(30000000L, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldThrowErrorWhenHearingStatusNotValid() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.AWAITING_LISTING.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.AWAITING_LISTING.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, 0);
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .hearingCompletion(hearingId, CLIENT_S2S_TOKEN));
            assertEquals(HEARING_ACTUALS_INVALID_STATUS, exception.getMessage());
        }

        @Test
        void shouldThrowErrorWhenHearingStatusIsListedAndMinStartDateIsBeforeNow() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, 1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .hearingCompletion(hearingId, CLIENT_S2S_TOKEN));
            assertEquals(HEARING_ACTUALS_INVALID_STATUS, exception.getMessage());
        }

        @Test
        void shouldThrowErrorWhenHearingOutcomeInformationNotAvailable() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                .thenReturn(Optional.empty());

            Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
                .hearingCompletion(hearingId, CLIENT_S2S_TOKEN));
            assertEquals(HEARING_ACTUALS_MISSING_HEARING_OUTCOME, exception.getMessage());
        }


        @Test
        void shouldThrowErrorWhenActualHearingDayNotPresentForActualHearing() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            ActualHearingEntity actualHearingEntity = mock(ActualHearingEntity.class);

            when(actualHearingEntity.getHearingResultType()).thenReturn(ADJOURNED);
            when(actualHearingEntity.getActualHearingIsFinalFlag()).thenReturn(Boolean.FALSE);
            when(actualHearingEntity.getActualHearingType()).thenReturn("TYPE 1");
            when(actualHearingEntity.getHearingResultReasonType()).thenReturn("REASON 1");

            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                .thenReturn(Optional.of(actualHearingEntity));
            Exception exception = assertThrows(BadRequestException.class, () ->
                    hearingManagementService.hearingCompletion(hearingId, CLIENT_S2S_TOKEN));

            assertTrue(exception.getMessage().contains(ValidationError.HA_OUTCOME_REQUEST_DATE_NOT_EMPTY));
        }

        @Test
        void shouldInvokeCompletionStatusWhenResultIsCompleted() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            ActualHearingEntity actualHearingEntity = mock(ActualHearingEntity.class);
            when(actualHearingEntity.getHearingResultType()).thenReturn(COMPLETED);
            when(actualHearingEntity.getActualHearingType()).thenReturn("HEARING TYPE TEST 1");
            when(actualHearingEntity.getHearingResultDate()).thenReturn(LocalDate.now().minusDays(3));
            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                .thenReturn(Optional.of(actualHearingEntity));
            mockHearingCompletionRequest();
            ResponseEntity responseEntity = hearingManagementService.hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        }

        @Test
        void shouldPassWhenActualHearingDayPresentForCancelledActualHearing() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            ActualHearingEntity actualHearingEntity = mock(ActualHearingEntity.class);
            when(actualHearingEntity.getHearingResultType()).thenReturn(CANCELLED);
            when(actualHearingEntity.getActualHearingType()).thenReturn("TYPE 32");
            when(actualHearingEntity.getHearingResultReasonType()).thenReturn("MADE UP REASON");
            when(actualHearingEntity.getHearingResultDate()).thenReturn(LocalDate.now().minusDays(13));
            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                .thenReturn(Optional.of(actualHearingEntity));
            verify(hearingStatusAuditService, times(1)).saveAuditTriageDetailsWithUpdatedDate(
                any(), any(), any(), any(), any(), any(), any());
            mockHearingCompletionRequest();
            hearingManagementService.hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
        }

        @Test
        void shouldPassWhenActualHearingDayPresentForNonCancelledActualHearing() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                    versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            ActualHearingEntity actualHearingEntity = mock(ActualHearingEntity.class);
            when(actualHearingEntity.getHearingResultType()).thenReturn(COMPLETED);
            when(actualHearingEntity.getActualHearingType()).thenReturn("TYPE 32");
            when(actualHearingEntity.getHearingResultReasonType()).thenReturn("MADE UP REASON");
            when(actualHearingEntity.getHearingResultDate()).thenReturn(LocalDate.now().minusDays(13));
            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                    .thenReturn(Optional.of(actualHearingEntity));
            mockHearingCompletionRequest();
            hearingManagementService.hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
            assertEquals(COMPLETED.getLabel(), hearingEntity.getStatus());
        }

        @Test
        void shouldInvokeUpdateCompletionStatusForActualHearing() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
            final int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
            when(hearingRepository.getStatus(hearingId)).thenReturn(PutHearingStatus.LISTED.name());
            HearingEntity hearingEntity = generateHearingEntity(hearingId, PutHearingStatus.LISTED.name(),
                                                                versionNumber
            );
            addHearingResponses(hearingEntity, 1, true, 1, -1);
            when(hearingRepository.findById(hearingId)).thenReturn(Optional.of(hearingEntity));
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            ActualHearingEntity actualHearingEntity = mock(ActualHearingEntity.class);
            when(actualHearingEntity.getHearingResultType()).thenReturn(CANCELLED);
            when(actualHearingEntity.getHearingResultDate()).thenReturn(LocalDate.now().minusDays(3));
            when(actualHearingEntity.getActualHearingType()).thenReturn("TYPE 22");
            when(actualHearingEntity.getHearingResultReasonType()).thenReturn("MADE UP REASON");
            when(actualHearingRepository.findByHearingResponse(any(HearingResponseEntity.class)))
                .thenReturn(Optional.of(actualHearingEntity));
            mockHearingCompletionRequest();
            ResponseEntity responseEntity = hearingManagementService.hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        }
    }

    @Nested
    @DisplayName("caseSearch")
    class CaseSearchResults {

        private static final String CASE_REF_1 = "1000100010001002";
        private static final String CASE_REF_2 = "2000200020002004";

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("caseReferences")
        void shouldGetCaseSearchResultsPaginated(List<String> caseReferences) {
            String elasticSearchQueryPaginated = getElasticSearchQueryPaginated(5, 5, caseReferences);

            List<DataStoreCaseDetails> dataStoreCaseDetailsList = new ArrayList<>();
            for (String caseReference : caseReferences) {
                DataStoreCaseDetails dataStoreCaseDetails =
                    DataStoreCaseDetails.builder()
                        .caseTypeId(CASE_TYPE).id(caseReference).jurisdiction(JURISDICTION)
                        .build();
                dataStoreCaseDetailsList.add(dataStoreCaseDetails);
            }

            CaseSearchResult caseSearchResult = new CaseSearchResult();
            caseSearchResult.setCases(dataStoreCaseDetailsList);

            when(dataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE, elasticSearchQueryPaginated))
                .thenReturn(caseSearchResult);

            List<DataStoreCaseDetails> returnedDataStoreCaseDetailsList =
                hearingManagementService.getCaseSearchResultsPaginated(5, 5, caseReferences, null, CASE_TYPE);

            assertNotNull(returnedDataStoreCaseDetailsList, "Case search results should not be null");
            assertEquals(caseReferences.size(),
                         returnedDataStoreCaseDetailsList.size(),
                         "Unexpected number of case search results");

            for (int index = 0; index < caseReferences.size(); index++) {
                assertDataStoreCaseDetails(returnedDataStoreCaseDetailsList.get(index), caseReferences.get(index));
            }

            verify(dataStoreRepository).findAllCasesByCaseIdUsingExternalApi(CASE_TYPE, elasticSearchQueryPaginated);
        }

        @Test
        void shouldGetCaseSearchResultsPaginatedNoneFound() {
            List<String> caseReferences = List.of(CASE_REF_1);
            String elasticSearchQueryPaginated = getElasticSearchQueryPaginated(10, 0, caseReferences);

            CaseSearchResult caseSearchResult = new CaseSearchResult();
            caseSearchResult.setCases(Collections.emptyList());

            when(dataStoreRepository.findAllCasesByCaseIdUsingExternalApi(CASE_TYPE, elasticSearchQueryPaginated))
                .thenReturn(caseSearchResult);

            List<DataStoreCaseDetails> returnedDataStoreCaseDetailsList =
                hearingManagementService.getCaseSearchResultsPaginated(10, 0, caseReferences, null, CASE_TYPE);

            assertNotNull(returnedDataStoreCaseDetailsList, "Case search results should not be null");
            assertEquals(0, returnedDataStoreCaseDetailsList.size(), "Unexpected number of case search results");
        }

        private String getElasticSearchQueryPaginated(Integer size, Integer from, List<String> caseReferences) {
            String commaSeparatedCaseReferences = "\"" + String.join("\", \"", caseReferences) + "\"";

            return """
                {
                    "size": %d,
                    "from": %d,
                    "query": {
                        "terms": {"reference": [%s]}
                    },
                    "sort": [
                        {"reference.keyword": "asc"}
                    ],
                    "_source": ["id", "jurisdiction", "case_type_id", "reference"]
                }""".formatted(size, from, commaSeparatedCaseReferences);
        }

        private void assertDataStoreCaseDetails(DataStoreCaseDetails dataStoreCaseDetails, String expectedCaseRef) {
            assertEquals(CASE_TYPE,
                         dataStoreCaseDetails.getCaseTypeId(),
                         "Case search result has unexpected case type id");
            assertEquals(expectedCaseRef,
                         dataStoreCaseDetails.getId(),
                         "Case search result has unexpected id");
            assertEquals(JURISDICTION,
                         dataStoreCaseDetails.getJurisdiction(),
                         "Case search result has unexpected jurisdiction");
        }

        private static Stream<Arguments> caseReferences() {
            return Stream.of(
                arguments(named("One case reference", List.of(CASE_REF_1))),
                arguments(named("Two case references", List.of(CASE_REF_1, CASE_REF_2)))
            );
        }
    }

    private void mockHearingCompletionRequest() {
        HmcHearingResponse hmcHearingResponse = new HmcHearingResponse();
        hmcHearingResponse.setHearingID("2000000000");
        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHmcStatus(HearingStatus.LISTED.name());

        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        when(hmiHearingResponseMapper.mapEntityToHmcModel(any(HearingResponseEntity.class),
                                                          any(HearingEntity.class))).thenReturn(hmcHearingResponse);
        when(objectMapperService.convertObjectToJsonNode(hmcHearingResponse)).thenReturn(jsonNode);
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
        caseHearingRequestEntity.setVersionNumber(versionNumber + 1);
        caseHearingRequestEntity.setCaseReference(CASE_REFERENCE);
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));
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
        return HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
    }

    private HmiDeleteHearingRequest getHmiDeleteHearingRequest() {
        return HmiDeleteHearingRequest.builder()
            .build();
    }

    private void addHearingResponses(HearingEntity hearingEntity,
                                     int noOfResponses,
                                     boolean addHearingDayDetails,
                                     int noOfHearingDayDetails,
                                     int noOfDays) {
        List<HearingResponseEntity> responseEntities = new ArrayList<>();
        for (int i = 0; i < noOfResponses; i++) {
            HearingResponseEntity responseEntity = new HearingResponseEntity();
            responseEntity.setRequestVersion(hearingEntity.getLatestRequestVersion());
            responseEntity.setRequestTimeStamp(LocalDateTime.now());
            responseEntities.add(responseEntity);
            if (addHearingDayDetails) {
                addHearingDayDetails(responseEntity, noOfHearingDayDetails, noOfDays);
            }
        }
        hearingEntity.setHearingResponses(responseEntities);
    }

    private void addHearingDayDetails(HearingResponseEntity responseEntity,
                                      int noOfHearingDayDetails,
                                      int noOfDays) {
        List<HearingDayDetailsEntity> hearingDayDetails = new ArrayList<>();
        for (int i = 0; i < noOfHearingDayDetails; i++) {
            HearingDayDetailsEntity entity = new HearingDayDetailsEntity();
            LocalDateTime startDate = LocalDateTime.now();
            startDate = startDate.plusDays(noOfDays);
            entity.setStartDateTime(startDate);
            hearingDayDetails.add(entity);
        }
        responseEntity.setHearingDayDetails(hearingDayDetails);
    }

    private void mockSubmitRequest() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetailsWithOrgType());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        HmiSubmitHearingRequest hmiSubmitHearingRequest = getHmiSubmitHearingRequest();
        when(hmiSubmitHearingRequestMapper.mapRequest(
            any(),
            any(),
            any()
        )).thenReturn(hmiSubmitHearingRequest);
        when(objectMapperService.convertObjectToJsonNode(hmiSubmitHearingRequest)).thenReturn(jsonNode);
    }

    private void mockGetEntities(HearingRequest hearingRequest) {
        EntitiesMapperObject entities = EntitiesMapperObject.builder()
            .entities(Collections.singletonList(Entity.builder().build()))
            .preferredHearingChannels(null)
            .build();
        when(entitiesMapper.getEntities(hearingRequest.getPartyDetails())).thenReturn(entities);
    }

    private HearingDetails buildHearingDetails() {
        HearingDetails hearingDetails = TestingUtil.hearingDetailsWithAllFields();
        hearingDetails.setDuration(365);
        return hearingDetails;
    }

    private HearingManagementServiceImpl createHearingManagementService() {
        return
            new HearingManagementServiceImpl(
                dataStoreRepository,
                hearingRepository,
                hearingMapper,
                caseHearingRequestRepository,
                hmiSubmitHearingRequestMapper,
                getHearingsResponseMapper,
                getHearingResponseMapper,
                messageSenderToTopicConfiguration,
                objectMapperService,
                hmiDeleteHearingRequestMapper,
                hearingIdValidator,
                linkedHearingValidator,
                partyRelationshipDetailsMapper,
                hearingActualsValidator,
                listingMapper,
                hmiCaseDetailsMapper,
                entitiesMapper,
                hmiHearingResponseMapper,
                hearingStatusAuditService,
                pendingRequestService);
    }
}
