package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class HearingResponseRepositoryTest {

    @Mock
    private HearingResponseRepository hearingResponseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHearingResponsesIsEmpty() {
        List<HearingResponseEntity> responsesDefault = new ArrayList<>();
        doReturn(responsesDefault).when(hearingResponseRepository).getHearingResponses(any());
        List<LocalDateTime> dateTimes = hearingResponseRepository.getHearingResponses(any());
        assertTrue(dateTimes.isEmpty());
    }

}
