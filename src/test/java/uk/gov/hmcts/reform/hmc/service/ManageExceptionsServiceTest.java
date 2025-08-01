package uk.gov.hmcts.reform.hmc.service;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_SUCCESS_MESSAGE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_CASEREF_MISMATCH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;

@ExtendWith(MockitoExtension.class)
class ManageExceptionsServiceTest {

    @InjectMocks
    private ManageExceptionsServiceImpl manageExceptionsService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    private ObjectMapper objectMapper;

    private static final String CLIENT_S2S_TOKEN = "hmc_tech_admin";

    private ManageExceptionRequest finalStateRequest;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        manageExceptionsService = new ManageExceptionsServiceImpl(
            hearingStatusAuditService, hearingRepository,
            objectMapper
        );
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(
            any(), any(), any(), any(),
            any(), any(), any());
        finalStateRequest = convertJsonToRequest("manage-exceptions/valid-final_state_transition_request.json");
    }

    @Nested
    @DisplayName("manageExceptions-Final State Transition")
    class ManageExceptions {
        @Test
        void shouldThrowExceptionWhenHearingIdLimitExceeds() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            List<SupportRequest> supportRequests = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setHearingId(String.valueOf(i));
                supportRequests.add(supportRequest);
            }
            request.setSupportRequests(supportRequests);
            Exception exception = assertThrows(
                HearingValidationException.class,
                () -> manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
            assertEquals(INVALID_HEARING_ID_LIMIT, exception.getMessage());

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
        }

        @Test
        void validateUniqueHearingIds_shouldThrowExceptionOnDuplicateIds() throws IOException {
            ManageExceptionRequest request = convertJsonToRequest("manage-exceptions/duplicate-hearingIds.json");
            Exception exception = assertThrows(
                HearingValidationException.class, () ->
                    manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
            assertEquals(DUPLICATE_HEARING_IDS, exception.getMessage());
        }

        @Test
        void validateAllHearingsSuccessfully()  {
            HearingEntity entity1 = TestingUtil.getHearingEntity(
                2000000000L, EXCEPTION.name(),
                "1742223756874235");
            HearingEntity entity2 = TestingUtil.getHearingEntity(
                2000000001L, EXCEPTION.name(),
                "1742223756874236");
            HearingEntity entity3 = TestingUtil.getHearingEntity(
                2000000002L, EXCEPTION.name(),
                "1742223756874237");

            List<HearingEntity> hearingEntities = List.of(entity1, entity2, entity3);
            List<Long> hearingIds = Arrays.asList(2000000000L, 2000000001L, 2000000002L);
            when(hearingRepository.getHearings(hearingIds))
                .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertEquals("2000000000", response.getSupportRequestResponse().get(0).getHearingId());
            assertEquals("2000000001", response.getSupportRequestResponse().get(1).getHearingId());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(0).getStatus());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(1).getStatus());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(0).getMessage());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(2).getMessage());
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            // To verify that the audit service is called 4 times
            verify(hearingStatusAuditService, times(4))
                .saveAuditTriageDetailsWithUpdatedDate(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void oneHearingDoesNotExistInDB() {
            HearingEntity entity1 = TestingUtil.getHearingEntity(
                2000000000L, EXCEPTION.name(), "1742223756874235");
            HearingEntity entity2 = TestingUtil.getHearingEntity(
                2000000001L, EXCEPTION.name(), "1742223756874236");
            HearingEntity entity3 = TestingUtil.getHearingEntity(
                2000000002L, EXCEPTION.name(), "1742223756874237");
            entity3.setId(2000000003L);
            List<HearingEntity> hearingEntities = List.of(entity1, entity2, entity3);
            List<Long> hearingIds = Arrays.asList(2000000000L, 2000000001L, 2000000002L);
            when(hearingRepository.getHearings(hearingIds))
                .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertEquals("2000000000", response.getSupportRequestResponse().get(0).getHearingId());
            assertEquals("2000000001", response.getSupportRequestResponse().get(1).getHearingId());
            assertEquals("2000000002", response.getSupportRequestResponse().get(2).getHearingId());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(0).getStatus());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(1).getStatus());
            assertEquals(ManageRequestStatus.FAILURE.label, response.getSupportRequestResponse().get(2).getStatus());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(0).getMessage());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(1).getMessage());
            assertEquals(INVALID_HEARING_ID, response.getSupportRequestResponse().get(2).getMessage());
            // To verify that the audit service is called 4 times
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            verify(hearingStatusAuditService, times(3))
                .saveAuditTriageDetailsWithUpdatedDate(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(2)).save(any(HearingEntity.class));
        }

        @Test
        void validateCaseReferenceForHearingId() {
            HearingEntity entity1 = TestingUtil.getHearingEntity(
                2000000000L, EXCEPTION.name(), "1742223756874235");
            HearingEntity entity2 = TestingUtil.getHearingEntity(
                2000000001L, EXCEPTION.name(), "1742223756874236");
            HearingEntity entity3 = TestingUtil.getHearingEntity(
                2000000002L, EXCEPTION.name(), "1742223756874238");
            List<HearingEntity> hearingEntities = List.of(entity1, entity2, entity3);
            List<Long> hearingIds = Arrays.asList(2000000000L, 2000000001L, 2000000002L);
            when(hearingRepository.getHearings(hearingIds))
                .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertEquals("2000000000", response.getSupportRequestResponse().get(0).getHearingId());
            assertEquals("2000000001", response.getSupportRequestResponse().get(1).getHearingId());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(0).getStatus());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(1).getStatus());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(0).getMessage());
            assertEquals(HEARING_ID_CASEREF_MISMATCH, response.getSupportRequestResponse().get(2).getMessage());
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            // To verify that the audit service is called 4 times
            verify(hearingStatusAuditService, times(4))
                .saveAuditTriageDetailsWithUpdatedDate(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }

        @Test
        void validateHearingStatusInExceptionState() {
            HearingEntity entity1 = TestingUtil.getHearingEntity(
                2000000000L, EXCEPTION.name(), "1742223756874235");
            HearingEntity entity2 = TestingUtil.getHearingEntity(
                2000000001L, EXCEPTION.name(), "1742223756874236");
            HearingEntity entity3 = TestingUtil.getHearingEntity(
                2000000002L, HEARING_REQUESTED.name(), "1742223756874237");

            List<HearingEntity> hearingEntities = List.of(entity1, entity2, entity3);
            List<Long> hearingIds = Arrays.asList(2000000000L, 2000000001L, 2000000002L);
            when(hearingRepository.getHearings(hearingIds))
                .thenReturn(hearingEntities);
            ManageExceptionResponse response = manageExceptionsService.manageExceptions(finalStateRequest,
                    CLIENT_S2S_TOKEN);
            assertEquals(3, response.getSupportRequestResponse().size());
            assertEquals("2000000000", response.getSupportRequestResponse().get(0).getHearingId());
            assertEquals("2000000001", response.getSupportRequestResponse().get(1).getHearingId());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(0).getStatus());
            assertEquals(ManageRequestStatus.SUCCESSFUL.label, response.getSupportRequestResponse().get(1).getStatus());
            assertEquals(MANAGE_EXCEPTION_SUCCESS_MESSAGE, response.getSupportRequestResponse().get(0).getMessage());
            assertEquals(ManageRequestStatus.FAILURE.label, response.getSupportRequestResponse().get(2).getStatus());
            assertEquals(INVALID_HEARING_ID_FINAL_STATE, response.getSupportRequestResponse().get(2).getMessage());
            verify(hearingRepository, times(1)).getHearings(hearingIds);
            // To verify that the audit service is called 4 times
            verify(hearingStatusAuditService, times(4))
                .saveAuditTriageDetailsWithUpdatedDate(any(), any(), any(), any(), any(), any(), any());
            verify(hearingRepository, times(3)).save(any(HearingEntity.class));
        }
    }

    private static ManageExceptionRequest convertJsonToRequest(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource resource = new ClassPathResource(filePath);
        return objectMapper.readValue(resource.getInputStream(), ManageExceptionRequest.class);
    }
}
