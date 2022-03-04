package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class HearingDayDetailsRepositoryTest {

    @Mock
    private HearingDayDetailsRepository hearingDayDetailsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        List<String> expectedHearingIds = Arrays.asList("2000000207", "2000000206", "2000000205");
        doReturn(expectedHearingIds).when(hearingDayDetailsRepository).getUnNotifiedHearingsWithOutStartDateTo(
            anyString(), any(), any());
        List<String> hearingIds = hearingDayDetailsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            anyString(),
            any(),
            any()
        );
        assertAll(
            () -> assertThat(hearingIds, is(expectedHearingIds)),
            () -> verify(hearingDayDetailsRepository, times(1)).getUnNotifiedHearingsWithOutStartDateTo(
                anyString(),
                any(),
                any()
            )
        );
    }

    @Test
    void testGetUnNotifiedHearingsWithStartDateTo() {
        List<String> expectedHearingIds = Arrays.asList("2000000207", "2000000206", "2000000205");
        doReturn(expectedHearingIds).when(hearingDayDetailsRepository).getUnNotifiedHearingsWithStartDateTo(
            anyString(), any(), any(),any());
        List<String> hearingIds = hearingDayDetailsRepository.getUnNotifiedHearingsWithStartDateTo(
            anyString(),
            any(),
            any(),
            any()
        );
        assertAll(
            () -> assertThat(hearingIds, is(expectedHearingIds)),
            () -> verify(hearingDayDetailsRepository, times(1)).getUnNotifiedHearingsWithStartDateTo(
                anyString(),
                any(),
                any(),
                any()
            )
        );
    }

    @Test
    void testGetUnNotifiedHearingsTotalCountWithStartDateTo() {
        Long expectedCount = 100L;
        doReturn(expectedCount).when(hearingDayDetailsRepository).getUnNotifiedHearingsTotalCountWithStartDateTo(
            anyString(), any(), any());
        Long count = hearingDayDetailsRepository.getUnNotifiedHearingsTotalCountWithStartDateTo(
            anyString(),
            any(),
            any()
        );
        assertAll(
            () -> assertThat(count, is(expectedCount)),
            () -> verify(hearingDayDetailsRepository, times(1)).getUnNotifiedHearingsTotalCountWithStartDateTo(
                anyString(),
                any(),
                any()
            )
        );
    }

    @Test
    void testGetUnNotifiedHearingsTotalCountWithOutStartDateTo() {
        Long expectedCount = 100L;
        doReturn(expectedCount).when(hearingDayDetailsRepository).getUnNotifiedHearingsTotalCountWithOutStartDateTo(
            anyString(), any());
        Long count = hearingDayDetailsRepository.getUnNotifiedHearingsTotalCountWithOutStartDateTo(anyString(),any());
        assertAll(
            () -> assertThat(count, is(expectedCount)),
            () -> verify(hearingDayDetailsRepository, times(1)).getUnNotifiedHearingsTotalCountWithOutStartDateTo(
                anyString(),
                any()
            )
        );
    }

}
