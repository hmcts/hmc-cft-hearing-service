package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class HearingChannelsRepositoryTest {

    @Mock
    private HearingChannelsRepository hearingChannelsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveHearingChannels() {
        HearingChannelsEntity hearingChannelsEntity = TestingUtil.hearingChannelsEntity().get(0);
        CaseHearingRequestEntity caseHearingRequestEntity = TestingUtil.caseHearingRequestEntity();
        caseHearingRequestEntity.setCaseHearingID(1L);
        hearingChannelsEntity.setCaseHearing(caseHearingRequestEntity);
        when(hearingChannelsRepository.save(any())).thenReturn(hearingChannelsEntity);
        HearingChannelsEntity savedEntity = hearingChannelsRepository.save(hearingChannelsEntity);
        assertEquals("someChannelType", savedEntity.getHearingChannelType());
        assertEquals(1L, savedEntity.getCaseHearing().getCaseHearingID());
    }
}