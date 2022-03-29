package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HearingRepositoryTest {

    @Mock
    private HearingRepository hearingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetVersionNumber() {
        String expectedStatus = "AWAITING_LISTING";
        doReturn("AWAITING_LISTING").when(hearingRepository).getStatus(any());
        String status = hearingRepository.getStatus(any());
        assertAll(
            () -> assertThat(status, is(expectedStatus)),
            () -> verify(hearingRepository, times(1)).getStatus(any())
        );
    }
}
