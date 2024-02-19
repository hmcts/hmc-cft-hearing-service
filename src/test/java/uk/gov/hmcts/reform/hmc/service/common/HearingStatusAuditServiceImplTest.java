package uk.gov.hmcts.reform.hmc.service.common;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.helper.HearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingServiceImpl;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC_TARGET;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI_TARGET;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

public class HearingStatusAuditServiceImplTest {

    @InjectMocks
    private HearingStatusAuditServiceImpl hearingStatusAuditService;

    @Mock
    HearingStatusAuditRepository hearingStatusAuditRepository;

    @Mock
    HearingStatusAuditMapper hearingStatusAuditMapper;

    @Mock
    SecurityUtils securityUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingStatusAuditService =
            new HearingStatusAuditServiceImpl(hearingStatusAuditMapper,
                                              hearingStatusAuditRepository,
                                              securityUtils);
    }

    @Nested
    @DisplayName("HearingStatusAudit")
    class HearingStatusAuditDetails {
        @Test
        void shouldSaveAuditTriageDetails() {
            given(securityUtils.getServiceNameFromS2SToken(HMC_TARGET)).willReturn(HMC_TARGET);
            given(hearingStatusAuditMapper.modelToEntity(TestingUtil.hearingStatusAudit())).willReturn
                (TestingUtil.hearingStatusAuditEntity());
            given(hearingStatusAuditRepository.save(TestingUtil.hearingStatusAuditEntity())).willReturn
                (TestingUtil.hearingStatusAuditEntity());
            hearingStatusAuditService. saveAuditTriageDetails("ABA1","2000000000",
                                                              "HEARING_REQUESTED", LocalDateTime.now(),
                                                              "create-hearing- request",HMC_TARGET,
                                                              HMI_TARGET,null,"1");
        }
    }
}
