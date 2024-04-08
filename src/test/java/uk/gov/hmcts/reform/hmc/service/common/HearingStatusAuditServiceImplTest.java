package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.helper.HearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LINKED_HEARING_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;

class HearingStatusAuditServiceImplTest {

    @InjectMocks
    private HearingStatusAuditServiceImpl hearingStatusAuditService;

    @Mock
    HearingStatusAuditRepository hearingStatusAuditRepository;

    @Mock
    HearingStatusAuditMapper hearingStatusAuditMapper;

    @Mock
    LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;

    @Mock
    LinkedHearingStatusAuditMapper linkedHearingStatusAuditMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingStatusAuditService =
            new HearingStatusAuditServiceImpl(hearingStatusAuditMapper,
                                              hearingStatusAuditRepository,
                                              linkedHearingStatusAuditMapper,
                                              linkedHearingStatusAuditRepository);
    }

    @Nested
    @DisplayName("HearingStatusAudit")
    class HearingStatusAuditDetails {
        @Test
        void shouldSaveAuditTriageDetailsWhenFailure() throws JsonProcessingException  {
            JsonNode errorDetails = new ObjectMapper().readTree("{\"deadLetterReason\":"
                                                                    + " \"MaxDeliveryCountExceeded \"}");
            given(hearingStatusAuditMapper.modelToEntity(TestingUtil.hearingStatusAudit(CREATE_HEARING_REQUEST)))
                .willReturn(TestingUtil.hearingStatusAuditEntity());
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            hearingStatusAuditService. saveAuditTriageDetails(TestingUtil.hearingEntity(), LocalDateTime.now(),
                                                              CREATE_HEARING_REQUEST,SUCCESS_STATUS,
                                                              HMC, HMI,errorDetails, HEARING_TYPE);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsWhenSuccess() {
            given(hearingStatusAuditMapper.modelToEntity(TestingUtil.hearingStatusAudit(CREATE_HEARING_REQUEST)))
                .willReturn(TestingUtil.hearingStatusAuditEntity());
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn(
                TestingUtil.hearingStatusAuditEntity());
            hearingStatusAuditService. saveAuditTriageDetails(TestingUtil.hearingEntity(), LocalDateTime.now(),
                                                              CREATE_HEARING_REQUEST,SUCCESS_STATUS,
                                                              HMC, HMI,null, HEARING_TYPE);
            verify(hearingStatusAuditRepository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("LinkedHearingStatusAudit")
    class LinkedHearingStatusAuditDetails {
        @Test
        void shouldSaveAuditTriageDetailsWhenFailure() throws JsonProcessingException  {
            JsonNode errorDetails = new ObjectMapper().readTree("{\"error\":"
                                                                    + " \"006 List Assist failed to respond \"}");
            given(linkedHearingStatusAuditMapper.modelToEntity(TestingUtil.hearingStatusAudit(
                CREATE_LINKED_HEARING_REQUEST))).willReturn(TestingUtil.linkedHearingStatusAuditEntity());
            given(linkedHearingStatusAuditRepository.save(TestingUtil.linkedHearingStatusAuditEntity())).willReturn(
                TestingUtil.linkedHearingStatusAuditEntity());
            hearingStatusAuditService. saveAuditTriageDetails(TestingUtil.hearingEntity(), LocalDateTime.now(),
                                                              CREATE_LINKED_HEARING_REQUEST,SUCCESS_STATUS,
                                                              HMC, HMI,errorDetails, LINKED_HEARING_TYPE);
            verify(linkedHearingStatusAuditRepository, times(1)).save(any());
        }

        @Test
        void shouldSaveAuditTriageDetailsWhenSuccess() {
            given(linkedHearingStatusAuditMapper.modelToEntity(TestingUtil.hearingStatusAudit(
                CREATE_LINKED_HEARING_REQUEST))).willReturn(TestingUtil.linkedHearingStatusAuditEntity());
            given(linkedHearingStatusAuditRepository.save(TestingUtil.linkedHearingStatusAuditEntity())).willReturn(
                TestingUtil.linkedHearingStatusAuditEntity());
            hearingStatusAuditService. saveAuditTriageDetails(TestingUtil.hearingEntity(), LocalDateTime.now(),
                                                              CREATE_LINKED_HEARING_REQUEST,SUCCESS_STATUS,
                                                              HMC, HMI,null, LINKED_HEARING_TYPE);
            verify(linkedHearingStatusAuditRepository, times(1)).save(any());
        }
    }
}
