package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidManageHearingServiceException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequestResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.security.idam.IdamRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.client.hmi.HearingCode.LISTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.IDAM_TECH_ADMIN_ROLE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_COMMIT_FAIL;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_COMMIT_FAIL_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TECH_ADMIN_UI_SERVICE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction.ROLLBACK;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASE_REF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_EXCEPTION_ROLE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_MANAGE_HEARING_SERVICE_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LAST_GOOD_STATE_EMPTY;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertJsonToRequest;

@ExtendWith(MockitoExtension.class)
class ManageExceptionsServiceTest {

    @InjectMocks
    private ManageExceptionsServiceImpl manageExceptionsService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SecurityUtils securityUtils;

    @Mock
    ApplicationParams applicationParams;

    @Mock
    IdamRepository idamRepository;

    private static final String CLIENT_S2S_TOKEN = "tech_admin_ui";

    private ManageExceptionRequest finalStateRequest;
    private ManageExceptionRequest rollBackRequest;

    private static final String SUCCESS_STATUS = ManageRequestStatus.SUCCESSFUL.label;
    private static final String FAILURE_STATUS = ManageRequestStatus.FAILURE.label;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        manageExceptionsService = new ManageExceptionsServiceImpl(
                hearingStatusAuditService, hearingRepository,
                objectMapper, securityUtils, applicationParams);
        lenient().doReturn(CLIENT_S2S_TOKEN).when(securityUtils).getServiceNameFromS2SToken(CLIENT_S2S_TOKEN);
        validServiceAndUserRole();
        finalStateRequest = convertJsonToRequest("manage-exceptions/valid-final_state_transition_request.json");
        rollBackRequest = convertJsonToRequest("manage-exceptions/valid-roll_back_request.json");
    }

    @Nested
    @DisplayName("manageExceptions-rollback-Final State Transition")
    class ManageExceptionsRollBackAndFinalStateTransition {

        @Test
        void validInvokingServiceAndUserRole() {
            List<HearingEntity> hearingEntities = createHearingEntitiesForFinalState();
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds)).thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            verify(securityUtils, times(1)).getUserInfo();
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(3))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void inValidInvokingService() {
            when(applicationParams.getAuthorisedSupportToolServices()).thenReturn(List.of("invalid_service"));
            Exception exception = assertThrows(
                    InvalidManageHearingServiceException.class, () ->
                            manageExceptionsService.manageExceptions(finalStateRequest, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_MANAGE_HEARING_SERVICE_EXCEPTION, exception.getMessage());
        }

        @Test
        void inValidInvokingUserRole() {
            when(applicationParams.getAuthorisedSupportToolRoles()).thenReturn(List.of("invalid_role"));
            Exception exception = assertThrows(
                    InvalidManageHearingServiceException.class, () ->
                            manageExceptionsService.manageExceptions(finalStateRequest, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_MANAGE_EXCEPTION_ROLE, exception.getMessage());
        }

        @Test
        void validateUniqueHearingIds_shouldThrowExceptionOnDuplicateIds() throws IOException {
            ManageExceptionRequest request = convertJsonToRequest("manage-exceptions/duplicate-hearingIds.json");
            Exception exception = assertThrows(
                BadRequestException.class, () ->
                            manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
            assertEquals(DUPLICATE_HEARING_IDS, exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenHearingIdLimitExceeds() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            List<SupportRequest> supportRequests = new ArrayList<>();
            List<Long> hearingIds = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setHearingId(String.valueOf(i));
                supportRequests.add(supportRequest);
            }
            supportRequests.forEach(supportRequest ->
                    hearingIds.add(Long.valueOf(supportRequest.getHearingId())));
            request.setSupportRequests(supportRequests);
            Exception exception = assertThrows(
                    BadRequestException.class,
                    () -> manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_HEARING_ID_LIMIT, exception.getMessage());
            verify(hearingRepository, times(0)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(0))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(0)).save(any(HearingEntity.class));
        }

        @Test
        void shouldNotThrowExceptionWhenHearingIdLimitIsWithinBounds() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            List<SupportRequest> supportRequests = new ArrayList<>();
            List<Long> hearingIds = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setHearingId("2000000000" + i);
                supportRequests.add(supportRequest);
            }
            supportRequests.forEach(supportRequest ->
                    hearingIds.add(Long.valueOf(supportRequest.getHearingId())));
            request.setSupportRequests(supportRequests);
            manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(0))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(0)).save(any(HearingEntity.class));
        }

        @Test
        void processSingle_returnsFailure_and_audits_when_commit_fails() throws Exception {
            HearingEntity entity = TestingUtil.getHearingEntity(2000000000L, EXCEPTION.name(),
                                                                "9856815055686759");
            entity.setLastGoodStatus(HearingStatus.LISTED.name());
            SupportRequest req = new SupportRequest();
            req.setHearingId("2000000000");
            req.setCaseRef("9856815055686759");
            req.setAction(ROLLBACK.label);
            req.setNotes("testing DB commit failure");

            doThrow(new RuntimeException("Db commit fail"))
                .when(hearingRepository).save(any(HearingEntity.class));
            when(objectMapper.convertValue(anyString(), eq(JsonNode.class)))
                .thenReturn(mock(JsonNode.class));

            Method m = ManageExceptionsServiceImpl.class
                .getDeclaredMethod("processSingle", HearingEntity.class, SupportRequest.class, String.class);
            m.setAccessible(true);
            ManageExceptionsServiceImpl service =
                new ManageExceptionsServiceImpl(hearingStatusAuditService, hearingRepository, objectMapper,
                                                securityUtils, applicationParams);
            SupportRequestResponse resp =
                (SupportRequestResponse) m.invoke(service, entity, req, TECH_ADMIN_UI_SERVICE);

            assertEquals(ManageRequestStatus.FAILURE.label, resp.getStatus());
            assertEquals(String.valueOf(entity.getId()), resp.getHearingId());
            assertEquals(MANAGE_EXCEPTION_COMMIT_FAIL, resp.getMessage());
            verify(hearingStatusAuditService).saveAuditTriageDetailsForSupportTools(
                eq(entity),
                eq(MANAGE_EXCEPTION_COMMIT_FAIL_EVENT),
                isNull(),
                eq(TECH_ADMIN_UI_SERVICE),
                eq(HMC),
                isNull(),
                any(JsonNode.class)
            );
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
        }
    }

    @Nested
    @DisplayName("manageExceptions-Final State Transition")
    class ManageExceptionsFinalStateTransition {

        @Test
        void validateUniqueHearingIds_shouldThrowExceptionOnDuplicateIds() throws IOException {
            ManageExceptionRequest request = convertJsonToRequest(
                    "manage-exceptions/duplicate-hearingIds-Final-Transition.json");
            Exception exception = assertThrows(
                    BadRequestException.class, () ->
                            manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN));
            assertEquals(DUPLICATE_HEARING_IDS, exception.getMessage());
        }

        @Test
        void validateAllHearingsSuccessfully() {
            List<HearingEntity> hearingEntities = createHearingEntitiesForFinalState();
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds)).thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(1).getState()));
            assertSupportRequestResponse(response, 2, "2000000002", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(2), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(2).getState()));

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(3))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void hearingDoesNotExistInDB() {
            List<HearingEntity> hearingEntities = createHearingEntitiesForFinalState();
            hearingEntities.get(2).setId(2000000003L);
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(1).getState()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    INVALID_HEARING_ID);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void validateCaseReferenceForHearingId() {
            List<HearingEntity> hearingEntities = createHearingEntitiesForFinalState();
            hearingEntities.get(2).getLatestCaseHearingRequest().setCaseReference("1742223756874238");
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(1).getState()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    HEARING_ID_CASE_REF_MISMATCH);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void hearingStatusNotInExceptionState_final_state_transition() {
            List<HearingEntity> hearingEntities = createHearingEntitiesForFinalState();
            hearingEntities.get(2).setStatus(HEARING_REQUESTED.name());
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(1).getState()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    INVALID_HEARING_STATE);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void invalidFinalStateTransitionRequest() throws IOException {
            List<Long> hearingIds = createHearingIds();
            List<HearingEntity> hearingEntities = createHearingEntities();
            when(hearingRepository.getHearings(hearingIds)).thenReturn(hearingEntities);
            ManageExceptionRequest request = convertJsonToRequest(
                    "manage-exceptions/inValid-final_state_transition_request.json");
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(request,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            finalStateRequest.getSupportRequests().get(0).getState()));
            assertSupportRequestResponse(response, 1, "2000000001", FAILURE_STATUS,
                    INVALID_HEARING_ID_FINAL_STATE);
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                                         HEARING_ID_CASE_REF_MISMATCH);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(1))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
        }
    }

    @Nested
    @DisplayName("manageExceptions-roll back")
    class ManageExceptionsRollBack {
        @Test
        void validateUniqueHearingIds_shouldThrowExceptionOnDuplicateIds() throws IOException {
            ManageExceptionRequest request = convertJsonToRequest(
                    "manage-exceptions/duplicate-hearingIds-roll-back.json");
            Exception exception = assertThrows(
                    BadRequestException.class, () ->
                            manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
            assertEquals(DUPLICATE_HEARING_IDS, exception.getMessage());
        }

        @Test
        void validateAllHearingsSuccessfully() {
            List<HearingEntity> hearingEntities = createHearingEntitiesWithLastGoodStatus();
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds)).thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            hearingEntities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            hearingEntities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(2), EXCEPTION.name(),
                            hearingEntities.get(2).getLastGoodStatus()));

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(3))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void hearingDoesNotExistInDB() {
            List<HearingEntity> hearingEntities = createHearingEntitiesWithLastGoodStatus();
            hearingEntities.get(2).setId(2000000003L);
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            hearingEntities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            hearingEntities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    INVALID_HEARING_ID);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void validateCaseReferenceForHearingId() {
            List<HearingEntity> hearingEntities = createHearingEntitiesWithLastGoodStatus();
            hearingEntities.get(2).getLatestCaseHearingRequest().setCaseReference("1742223756874238");
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());

            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(0), EXCEPTION.name(),
                            hearingEntities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(hearingEntities.get(1), EXCEPTION.name(),
                            hearingEntities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    HEARING_ID_CASE_REF_MISMATCH);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void validRollBackRequest() {
            List<Long> hearingIds = createHearingIds();
            List<HearingEntity> entities = createHearingEntitiesWithLastGoodStatus();

            when(hearingRepository.getHearings(hearingIds)).thenReturn(entities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(0), EXCEPTION.name(), entities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(1),EXCEPTION.name(), entities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(2), EXCEPTION.name(), entities.get(2).getLastGoodStatus()));

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(3))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void noLastGoodStatusForRollBackRequest() {
            List<Long> hearingIds = createHearingIds();
            List<HearingEntity> entities = createHearingEntities();
            entities.get(0).setLastGoodStatus(HEARING_REQUESTED.name());
            entities.get(1).setLastGoodStatus(ADJOURNED.name());
            when(hearingRepository.getHearings(hearingIds)).thenReturn(entities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", FAILURE_STATUS,
                   INVALID_LAST_GOOD_STATE);
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(1), EXCEPTION.name(),
                            entities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                                         LAST_GOOD_STATE_EMPTY);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(1))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
        }

        @Test
        void stateProvidedForRollBackRequest() throws IOException {
            List<Long> hearingIds = createHearingIds();
            List<HearingEntity> entities = createHearingEntities();
            entities.get(0).setLastGoodStatus(COMPLETED.name());
            entities.get(1).setLastGoodStatus(ADJOURNED.name());
            when(hearingRepository.getHearings(hearingIds)).thenReturn(entities);
            ManageExceptionRequest request = convertJsonToRequest(
                "manage-exceptions/inValid-roll_back_request.json");
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(request,
                                                                                        CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                                         createExpectedMessage(entities.get(0), EXCEPTION.name(),
                                                               entities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", FAILURE_STATUS, INVALID_STATE);
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                                         LAST_GOOD_STATE_EMPTY);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(1))
                .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(1)).save(any(HearingEntity.class));
        }

        @Test
        void hearingStatusNotInExceptionState_roll_back_request() {
            List<HearingEntity> entities = createHearingEntitiesWithLastGoodStatus();
            entities.get(2).setStatus(HEARING_REQUESTED.name());
            List<Long> hearingIds = createHearingIds();
            when(hearingRepository.getHearings(hearingIds))
                    .thenReturn(entities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(rollBackRequest,
                    CLIENT_S2S_TOKEN);

            assertEquals(3, response.getSupportRequestResponse().size());
            assertSupportRequestResponse(response, 0, "2000000000", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(0), EXCEPTION.name(), entities.get(0).getLastGoodStatus()));
            assertSupportRequestResponse(response, 1, "2000000001", SUCCESS_STATUS,
                    createExpectedMessage(entities.get(1), EXCEPTION.name(), entities.get(1).getLastGoodStatus()));
            assertSupportRequestResponse(response, 2, "2000000002", FAILURE_STATUS,
                    INVALID_HEARING_STATE);

            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(2))
                    .saveAuditTriageDetailsForSupportTools(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }
    }

    private List<HearingEntity> createHearingEntities() {
        return List.of(
                TestingUtil.getHearingEntity(2000000000L, EXCEPTION.name(), "9856815055686759"),
                TestingUtil.getHearingEntity(2000000001L, EXCEPTION.name(), "9856815055686759"),
                TestingUtil.getHearingEntity(2000000002L, EXCEPTION.name(), "9372710950276233"));
    }

    private List<HearingEntity> createHearingEntitiesForFinalState() {
        return List.of(
            TestingUtil.getHearingEntityForFinalState(2000000000L, EXCEPTION.name(), "9856815055686759"),
            TestingUtil.getHearingEntityForFinalState(2000000001L, EXCEPTION.name(), "9856815055686759"),
            TestingUtil.getHearingEntityForFinalState(2000000002L, EXCEPTION.name(), "9372710950276233"));
    }

    private List<Long> createHearingIds() {
        return Arrays.asList(2000000000L, 2000000001L, 2000000002L);
    }

    private void assertSupportRequestResponse(ManageExceptionResponse response, int index,
                                              String hearingId, String status, String message) {
        assertEquals(hearingId, response.getSupportRequestResponse().get(index).getHearingId());
        assertEquals(status, response.getSupportRequestResponse().get(index).getStatus());
        assertEquals(message, response.getSupportRequestResponse().get(index).getMessage());
    }

    private List<HearingEntity> createHearingEntitiesWithLastGoodStatus() {
        List<HearingEntity> entities = createHearingEntities();
        entities.get(0).setLastGoodStatus(COMPLETED.name());
        entities.get(1).setLastGoodStatus(ADJOURNED.name());
        entities.get(2).setLastGoodStatus(LISTED.name());
        return entities;
    }

    private String createExpectedMessage(HearingEntity entity, String oldStatus, String newStatus) {
        return String.format(
                MANAGE_EXCEPTION_SUCCESS_MESSAGE,
                entity.getId(),
                oldStatus,
                newStatus);
    }

    private void validServiceAndUserRole() {
        lenient().when(applicationParams.getAuthorisedSupportToolRoles()).thenReturn(List.of(IDAM_TECH_ADMIN_ROLE));
        lenient().when(applicationParams.getAuthorisedSupportToolServices()).thenReturn(List.of(TECH_ADMIN_UI_SERVICE));
        UserInfo userInfo = mock(UserInfo.class);
        lenient().when(userInfo.getRoles()).thenReturn(List.of("hmc_tech_admin"));
        lenient().doReturn(userInfo).when(securityUtils).getUserInfo();
    }

}
