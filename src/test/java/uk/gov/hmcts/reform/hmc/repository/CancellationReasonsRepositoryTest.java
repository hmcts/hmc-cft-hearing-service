package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CancellationReasonsRepositoryTest {

    @Mock
    private CancellationReasonsRepository cancellationReasonsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCancellationReason() {
        CancellationReasonsEntity cancellationReasonsEntity = new CancellationReasonsEntity();
        cancellationReasonsEntity.setCancellationReasonType("Completed");
        CaseHearingRequestEntity caseHearingRequestEntity = TestingUtil.caseHearingRequestEntity();
        caseHearingRequestEntity.setCaseHearingID(1L);
        cancellationReasonsEntity.setCaseHearing(caseHearingRequestEntity);
        when(cancellationReasonsRepository.save(any())).thenReturn(cancellationReasonsEntity);
        CancellationReasonsEntity savedEntity = cancellationReasonsRepository.save(cancellationReasonsEntity);
        assertEquals("Completed", savedEntity.getCancellationReasonType());
        assertEquals(1L, savedEntity.getCaseHearing().getCaseHearingID());
    }

}
