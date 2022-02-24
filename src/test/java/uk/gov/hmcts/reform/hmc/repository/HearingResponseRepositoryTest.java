package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.PartiesNotifiedCommonGeneration;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class HearingResponseRepositoryTest extends PartiesNotifiedCommonGeneration {

    @Mock
    private HearingResponseRepository hearingResponseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHearingResponsesIsEmpty() {
        List<HearingResponseEntity> responsesDefault = new ArrayList<>();
        doReturn(responsesDefault).when(hearingResponseRepository).getPartiesNotified(any());
        List<HearingResponseEntity> responseEntities = hearingResponseRepository.getPartiesNotified(any());
        assertTrue(responseEntities.isEmpty());
    }

    @Test
    void getHearingResponsesIsNotEmpty() {
        Long hearingId = 2000000099L;
        List<HearingResponseEntity> partiesNotifiedDefault = generateEntities(hearingId);
        doReturn(partiesNotifiedDefault).when(hearingResponseRepository).getPartiesNotified(any());
        List<HearingResponseEntity> partiesNotified = hearingResponseRepository.getPartiesNotified(any());
        assertFalse(partiesNotified.isEmpty());
        assertEquals(3, partiesNotified.size());
    }

}
