package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ChangeReasonsEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChangeReasonsRepositoryTest {

    @Mock
    private ChangeReasonsRepository changeReasonsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveChangeReason() {

        final var changeReason = "Something Changed";
        final var caseHearingId = 1L;
        ChangeReasonsEntity changeReasonsEntity = new ChangeReasonsEntity();
        changeReasonsEntity.setChangeReasonType(changeReason);

        CaseHearingRequestEntity caseHearingRequestEntity = TestingUtil.caseHearingRequestEntity();
        caseHearingRequestEntity.setCaseHearingID(caseHearingId);
        changeReasonsEntity.setCaseHearing(caseHearingRequestEntity);

        when(changeReasonsRepository.save(any())).thenReturn(changeReasonsEntity);

        ChangeReasonsEntity savedEntity = changeReasonsRepository.save(changeReasonsEntity);
        assertEquals(changeReason, savedEntity.getChangeReasonType());
        assertEquals(1L, savedEntity.getCaseHearing().getCaseHearingID());
    }

}
