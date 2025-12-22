package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MANAGE_EXCEPTION_AUDIT_EVENT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TECH_ADMIN_UI_SERVICE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;

class HearingStatusAuditServiceImplTest {

    @InjectMocks
    private HearingStatusAuditServiceImpl hearingStatusAuditService;

    @Mock
    HearingStatusAuditRepository hearingStatusAuditRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingStatusAuditService =
            new HearingStatusAuditServiceImpl(hearingStatusAuditRepository);
    }

    @Nested
    class HearingStatusAuditDetails {
        @Test
        void shouldSaveAuditTriageDetailsWhenFailure() throws JsonProcessingException  {
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
            hearingStatusAuditService.saveAuditTriageDetailsForSupportTools(hearingEntity,
                                                                            MANAGE_EXCEPTION_AUDIT_EVENT,
                                                                            null, TECH_ADMIN_UI_SERVICE,
                                                                            HMC,null, otherInfo);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsWithUpdatedDateToNow() throws JsonProcessingException  {
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            HearingEntity hearingEntity = TestingUtil.hearingEntity();
            hearingEntity.setStatus(COMPLETED.toString());
            hearingEntity.setCreatedDateTime(LocalDateTime.now());
            hearingEntity.setUpdatedDateTime(LocalDateTime.now());
            final JsonNode otherInfo = new ObjectMapper().readTree("{\"INCNUMBER\":"
                                                                       + " \"219876 : Final Transition\"}");
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateToNow(hearingEntity,
                                                                            MANAGE_EXCEPTION_AUDIT_EVENT,
                                                                            null, TECH_ADMIN_UI_SERVICE,
                                                                            HMC,null, otherInfo);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }
    }
}
