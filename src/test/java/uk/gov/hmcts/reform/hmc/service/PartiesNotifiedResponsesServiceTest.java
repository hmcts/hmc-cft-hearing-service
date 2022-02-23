package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.PartiesNotifiedCommonGeneration;
import uk.gov.hmcts.reform.hmc.data.PartiesNotifiedEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedResponsesServiceTest extends PartiesNotifiedCommonGeneration {

    @InjectMocks
    PartiesNotifiedServiceImpl partiesNotifiedService;

    @Mock
    HearingResponseRepository hearingResponseRepository;

    @Mock
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        partiesNotifiedService =
                new PartiesNotifiedServiceImpl(hearingRepository,
                        hearingResponseRepository);
    }

    @Test
    void shouldFindPartiesNotifiedForValidHearingId() {
        final Long hearingId = 2000000001L;
        List<PartiesNotifiedEntity> partiesNotifiedAnswer = generateEntities(hearingId);

        when(hearingRepository.existsById(hearingId)).thenReturn(true);
        when(hearingResponseRepository.getPartiesNotified(any())).thenReturn(partiesNotifiedAnswer);

        PartiesNotifiedResponses partiesNotifiedResponses =
                partiesNotifiedService.getPartiesNotified(hearingId);
        assertFalse(partiesNotifiedResponses.getResponses().isEmpty());
        assertEquals(3, partiesNotifiedResponses.getResponses().size());
    }

    @Test
    void shouldNotFindPartiesNotifiedForValidHearingId() {
        final Long hearingId = 2000000001L;
        List<PartiesNotifiedEntity> partiesNotifiedDateTimesAnswer = new ArrayList<>();
        when(hearingRepository.existsById(hearingId)).thenReturn(true);
        when(hearingResponseRepository.getPartiesNotified(hearingId)).thenReturn(partiesNotifiedDateTimesAnswer);
        PartiesNotifiedResponses partiesNotifiedDateTimes = partiesNotifiedService.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.getResponses().isEmpty());
    }

    @Test
    void shouldFindErrorForNullHearingId() {
        final Long hearingId = null;
        shouldFindErrorForInvalidHearingId(hearingId);
    }

    @Test
    void shouldFindErrorForInvalidFormatHearingId() {
        final Long hearingId = 1000000001L;
        shouldFindErrorForInvalidHearingId(hearingId);
    }

    @Test
    void shouldFindErrorForHearingIdNotFound() {
        final Long hearingId = 2000000001L;
        when(hearingRepository.existsById(hearingId)).thenReturn(false);
        Exception exception = assertThrows(HearingNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(hearingId)
        );
        assertEquals("No hearing found for reference: " + hearingId, exception.getMessage());
    }

    private void shouldFindErrorForInvalidHearingId(Long hearingId) {
        Exception exception = assertThrows(BadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(hearingId)
        );
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

}
