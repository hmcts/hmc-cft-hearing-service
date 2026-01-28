package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_ACK;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_RESPONSE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_SUCCESS_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_COMMIT_FAIL_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_PARTIES_NOTIFIED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UPDATE_HEARING_REQUEST;

class HearingStatusAuditServiceImplTest {

    @InjectMocks
    private HearingStatusAuditServiceImpl hearingStatusAuditService;

    @Mock
    HearingStatusAuditRepository hearingStatusAuditRepository;

    @Captor
    private ArgumentCaptor<HearingStatusAuditEntity> hearingStatusAuditEntityCaptor;

    final String clientS2SToken = "Test Service";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingStatusAuditService =
            new HearingStatusAuditServiceImpl(hearingStatusAuditRepository);
    }

    @Nested
    class ValidateSaveAuditTriageDetailsWithCreatedDate {
        @Test
        void auditChangeInRequestVersion() throws JsonProcessingException {
            HearingEntity hearingEntity = getHearingEntity();

            final JsonNode otherInfo = new ObjectMapper().readTree("{\"" + REQUEST_VERSION_UPDATE + "\":"
                                                                       + " \"requestVersion set to 1 \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(REQUEST_VERSION_UPDATE)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .otherInfo(otherInfo)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(context);
            assertSaveAuditTriageDetailsWithCreatedDate(context);
        }

        @Test
        void insertHearingRequest() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(CREATE_HEARING_REQUEST)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(context);
            assertSaveAuditTriageDetailsWithCreatedDate(context);
        }

        private void assertSaveAuditTriageDetailsWithCreatedDate(HearingStatusAuditContext context) {
            HearingStatusAuditEntity savedEntity = getHearingStatusAuditEntity();
            assertEquals(
                context.getHearingEntity().getCreatedDateTime(), savedEntity.getStatusUpdateDateTime());
        }

    }

    @Nested
    class ValidateSaveAuditTriageDetailsForSupportTools {
        @Test
        void manageExceptionWhenSuccess() throws JsonProcessingException {
            HearingEntity hearingEntity = getHearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);

            final JsonNode otherInfo = new ObjectMapper().readTree(" \"INC-XXX \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(MANAGE_EXCEPTION_AUDIT_EVENT)
                    .source(HMI)
                    .target(HMC)
                    .useCurrentTimestamp(true)
                    .otherInfo(otherInfo)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(context);
            assertAuditDetailsForSupportTools(startDateTime, startDateTimePlusFiveMinutes);
        }

        @Test
        void manageExceptionWhenFailure() throws JsonProcessingException {
            HearingEntity hearingEntity = getHearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);

            final JsonNode otherInfo = new ObjectMapper().readTree(" \"DB commit failed \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(MANAGE_EXCEPTION_COMMIT_FAIL_EVENT)
                    .source(HMI)
                    .target(HMC)
                    .useCurrentTimestamp(true)
                    .otherInfo(otherInfo)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(context);
            assertAuditDetailsForSupportTools(startDateTime, startDateTimePlusFiveMinutes);
        }

        private void assertAuditDetailsForSupportTools(LocalDateTime startDateTime,
                                                                  LocalDateTime startDateTimePlusFiveMinutes) {
            HearingStatusAuditEntity savedEntity = getHearingStatusAuditEntity();
            assertTrue(savedEntity.getStatusUpdateDateTime().isAfter(startDateTime)
                           && savedEntity.getStatusUpdateDateTime().isBefore(startDateTimePlusFiveMinutes));
            assertNull(savedEntity.getResponseDateTime());
        }

    }

    @Nested
    class ValidateAuditTriageDetailsWithUpdatedDateOrCurrentDate {

        @Test
        void upsertNewHearingActuals() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(PUT_HEARING_ACTUALS_COMPLETION)
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void updateHearingRequest() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(UPDATE_HEARING_REQUEST)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void deleteHearingRequest() {
            HearingEntity hearingEntity = getHearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);

            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(DELETE_HEARING_REQUEST)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(true)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsTrue(startDateTime, startDateTimePlusFiveMinutes);
        }

        @Test
        void hearingCompletion() {
            HearingEntity hearingEntity = getHearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);

            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(POST_HEARING_ACTUALS_COMPLETION)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(true)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsTrue(startDateTime, startDateTimePlusFiveMinutes);
        }

        @Test
        void auditChangeInRequestVersion_TimeStamp_False() throws JsonProcessingException {
            HearingEntity hearingEntity = getHearingEntity();

            final JsonNode otherInfo = new ObjectMapper().readTree("{\"" + REQUEST_VERSION_UPDATE + "\":"
                                                                       + " \"updated from 1 to 2 \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(REQUEST_VERSION_UPDATE)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .otherInfo(otherInfo)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void auditChangeInRequestVersion_TimeStamp_True() throws JsonProcessingException {
            HearingEntity hearingEntity = getHearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);
            final JsonNode otherInfo = new ObjectMapper().readTree("{\"" + REQUEST_VERSION_UPDATE + "\":"
                                                                       + " \"updated from 1 to 2 \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(REQUEST_VERSION_UPDATE)
                    .httpStatus(String.valueOf(HttpStatus.OK.value()))
                    .source(clientS2SToken)
                    .target(HMC)
                    .otherInfo(otherInfo)
                    .useCurrentTimestamp(true)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsTrue(startDateTime, startDateTimePlusFiveMinutes);
        }

        @Test
        void getPartiesNotified() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(PUT_PARTIES_NOTIFIED)
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void inboundUpdateHearingFailure() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(LA_RESPONSE)
                    .httpStatus(LA_FAILURE_STATUS)
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void inboundUpdateHearingSuccess() {
            HearingEntity hearingEntity = getHearingEntity();
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(HEARING_FINAL_STATE)
                    .httpStatus(LA_SUCCESS_STATUS)
                    .source(clientS2SToken)
                    .target(HMC)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());;
        }

        @Test
        void inboundUpdateHearingSyncResponse() throws JsonProcessingException {

            HearingEntity hearingEntity = getHearingEntity();
            final JsonNode errorDescription = new ObjectMapper().readTree(" \"Failure \"}");
            HearingStatusAuditContext context =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(LA_ACK)
                    .httpStatus(LA_SUCCESS_STATUS)
                    .source(HMC)
                    .target(FH)
                    .errorDetails(errorDescription)
                    .useCurrentTimestamp(false)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            assertAuditDetailsWhenCurrentTimeStampIsFalse(context.getHearingEntity().getUpdatedDateTime());
        }

        private void assertAuditDetailsWhenCurrentTimeStampIsFalse(LocalDateTime updatedDateTime) {
            HearingStatusAuditEntity savedEntity = getHearingStatusAuditEntity();
            assertEquals(updatedDateTime, savedEntity.getStatusUpdateDateTime());

        }

        private void assertAuditDetailsWhenCurrentTimeStampIsTrue(LocalDateTime startDateTime,
                                                                  LocalDateTime startDateTimePlusFiveMinutes) {
            HearingStatusAuditEntity savedEntity = getHearingStatusAuditEntity();
            assertTrue(savedEntity.getStatusUpdateDateTime().isAfter(startDateTime)
                           && savedEntity.getStatusUpdateDateTime().isBefore(startDateTimePlusFiveMinutes));
        }
    }

    private HearingStatusAuditEntity getHearingStatusAuditEntity() {
        verify(hearingStatusAuditRepository).save(hearingStatusAuditEntityCaptor.capture());
        HearingStatusAuditEntity savedEntity = hearingStatusAuditEntityCaptor.getValue();
        return savedEntity;
    }

    private static HearingEntity getHearingEntity() {
        HearingEntity hearingEntity = TestingUtil.getHearingEntity(
            2000000000L, REQUEST_VERSION_UPDATE,
            "9856815055686759"
        );
        hearingEntity.setCreatedDateTime(LocalDate.now().minusDays(3).atStartOfDay());
        hearingEntity.setUpdatedDateTime(LocalDate.now().minusDays(2).atStartOfDay());
        return hearingEntity;
    }

}

