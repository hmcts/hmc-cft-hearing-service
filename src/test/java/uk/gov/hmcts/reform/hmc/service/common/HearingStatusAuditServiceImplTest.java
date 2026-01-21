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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TECH_ADMIN_UI_SERVICE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;

class HearingStatusAuditServiceImplTest {

    @InjectMocks
    private HearingStatusAuditServiceImpl hearingStatusAuditService;

    @Mock
    HearingStatusAuditRepository hearingStatusAuditRepository;

    @Captor
    private ArgumentCaptor<HearingStatusAuditEntity> hearingStatusAuditEntityCaptor;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingStatusAuditService =
            new HearingStatusAuditServiceImpl(hearingStatusAuditRepository);
    }

    @Nested
    class HearingStatusAuditDetails {
        @Test
        void shouldSaveAuditTriageDetailsWhenFailure() throws JsonProcessingException {
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            HearingEntity hearingEntity = TestingUtil.hearingEntity();
            hearingEntity.setCreatedDateTime(LocalDateTime.now());
            hearingEntity.setUpdatedDateTime(LocalDateTime.now());
            final JsonNode errorDetails = new ObjectMapper().readTree("{\"deadLetterReason\":"
                                                                          + " \"MaxDeliveryCountExceeded \"}");
            hearingStatusAuditService. saveAuditTriageDetailsWithUpdatedDate(hearingEntity,
                                                              CREATE_HEARING_REQUEST,LA_FAILURE_STATUS,
                                                              HMC, HMI,errorDetails);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsWhenSuccess() {
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            HearingEntity hearingEntity = TestingUtil.hearingEntity();
            hearingEntity.setCreatedDateTime(LocalDateTime.now());
            hearingEntity.setUpdatedDateTime(LocalDateTime.now());
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingEntity,
                                                              CREATE_HEARING_REQUEST,SUCCESS_STATUS,
                                                              HMC, HMI,null);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsWithOtherInfoWhenSuccess() throws JsonProcessingException  {
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            HearingEntity hearingEntity = TestingUtil.hearingEntity();
            hearingEntity.setCreatedDateTime(LocalDateTime.now());
            hearingEntity.setUpdatedDateTime(LocalDateTime.now());
            final JsonNode otherInfo = new ObjectMapper().readTree("{\"detail\":"
                                                                       + " \"requestVersion starts at 1\"}");
            hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(hearingEntity,
                                                              CREATE_HEARING_REQUEST, REQUEST_VERSION_UPDATE,
                                                              HMC, HMI,null,otherInfo);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsForSupportTools() throws JsonProcessingException  {
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            HearingEntity hearingEntity = TestingUtil.hearingEntity();
            hearingEntity.setStatus(COMPLETED.toString());
            hearingEntity.setCreatedDateTime(LocalDateTime.now());
            hearingEntity.setUpdatedDateTime(LocalDateTime.now());
            final JsonNode otherInfo = new ObjectMapper().readTree("{\"INCNUMBER\":"
                                                                       + " \"219876 : Final Transition\"}");
            HearingStatusAuditContext context = HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(MANAGE_EXCEPTION_AUDIT_EVENT)
                .source(TECH_ADMIN_UI_SERVICE)
                .target(HMC)
                .otherInfo(otherInfo)
                .build();
            hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(context);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetails_WhenUseCurrentTimestampIsTrue() {
            assertAuditTriageTimestamp(true, DELETE_HEARING_REQUEST);
        }

        @Test
        void shouldSaveAuditTriageDetails_WhenUseCurrentTimestampIsFalse() {
            assertAuditTriageTimestamp(false, PUT_HEARING_ACTUALS_COMPLETION);
        }

        private void assertAuditTriageTimestamp(boolean useCurrentTimestamp, String hearingEvent) {
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime startDateTimePlusFiveMinutes = startDateTime.plusMinutes(5);

            HearingEntity hearingEntity = TestingUtil.getHearingEntity(
                2000000000L, hearingEvent,
                "9856815055686759"
            );
            hearingEntity.setCreatedDateTime(LocalDate.now().minusDays(3).atStartOfDay());
            hearingEntity.setUpdatedDateTime(LocalDate.now().minusDays(2).atStartOfDay());

            HearingStatusAuditContext context = HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(hearingEvent)
                .httpStatus(String.valueOf(HttpStatus.OK.value()))
                .source("Test Service")
                .target(HMC)
                .useCurrentTimestamp(useCurrentTimestamp)
                .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(context);
            verify(hearingStatusAuditRepository).save(hearingStatusAuditEntityCaptor.capture());
            HearingStatusAuditEntity savedEntity = hearingStatusAuditEntityCaptor.getValue();
            if (useCurrentTimestamp) {
                assertTrue(savedEntity.getStatusUpdateDateTime().isAfter(startDateTime)
                               && savedEntity.getStatusUpdateDateTime().isBefore(startDateTimePlusFiveMinutes));
            } else {
                assertEquals(
                    hearingEntity.getUpdatedDateTime(), savedEntity.getStatusUpdateDateTime());
            }
        }
    }
}

