package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedServiceTest {

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
        List<LocalDateTime> partiesNotifiedDateTimesAnswer = new ArrayList<>();
        partiesNotifiedDateTimesAnswer.add(LocalDateTime.now().minusDays(6));
        partiesNotifiedDateTimesAnswer.add(LocalDateTime.now().minusDays(4));

        when(hearingRepository.existsById(hearingId)).thenReturn(true);
        when(hearingResponseRepository.getHearingResponses(any())).thenReturn(partiesNotifiedDateTimesAnswer);

        List<LocalDateTime> partiesNotifiedDateTimes =
                partiesNotifiedService.getPartiesNotified(hearingId);
        assertFalse(partiesNotifiedDateTimes.isEmpty());
        assertEquals(2, partiesNotifiedDateTimes.size());
    }

    @Test
    void shouldNotFindPartiesNotifiedForValidHearingId() {
        final Long hearingId = 2000000001L;
        List<LocalDateTime> partiesNotifiedDateTimesAnswer = new ArrayList<>();
        when(hearingRepository.existsById(hearingId)).thenReturn(true);
        when(hearingResponseRepository.getHearingResponses(hearingId)).thenReturn(partiesNotifiedDateTimesAnswer);
        List<LocalDateTime> partiesNotifiedDateTimes = partiesNotifiedService.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.isEmpty());
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
