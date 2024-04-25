package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertDateTime;

class UnNotifiedHearingsRepositoryImplTest {

    @Mock
    private UnNotifiedHearingsRepositoryImpl unNotifiedHearingsRepository;

    List<String> hearingStatusListed  = List.of("LISTED");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000001L, 2100000000L);
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        when(unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(any(), any(), any()))
            .thenReturn(expectedHearingIds);
        List<Long> hearingIds = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "Test", startFrom, hearingStatusListed);
        assertNotNull(hearingIds.size());
        assertEquals(2, hearingIds.size());
        assertTrue(expectedHearingIds.containsAll(hearingIds));
    }

    @Test
    void testGetUnNotifiedHearingsWithStartDateTo() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000001L, 2100000000L);
        String dateStr = "2019-12-10 11:00:00";
        String dateStrTo = "2021-10-01 11:00:00";
        LocalDateTime startTo = convertDateTime(dateStrTo);
        LocalDateTime startFrom = convertDateTime(dateStr);
        when(unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(any(), any(), any(), any()))
            .thenReturn(expectedHearingIds);
        List<Long> hearingIds = unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
            "Test", startFrom, startTo, hearingStatusListed);
        assertNotNull(hearingIds.size());
        assertEquals(2, hearingIds.size());
        assertTrue(expectedHearingIds.containsAll(hearingIds));
    }

    @Test
    void testGetUnNotifiedHearingsWithOutStartDateToForCancelled() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000004L);
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<String> hearingStatusCancelled  = List.of("Cancelled");
        when(unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(any(), any(), any()))
            .thenReturn(expectedHearingIds);
        List<Long> hearingIds = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "Test", startFrom, hearingStatusCancelled);
        assertNotNull(hearingIds.size());
        assertEquals(1, hearingIds.size());
        assertTrue(expectedHearingIds.containsAll(hearingIds));
    }

    @Test
    void testGetUnNotifiedHearingsWithOutStartDateToForMultiStatus() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000004L);
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<String> hearingStatus  = List.of("Cancelled", "LISTED");
        when(unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(any(), any(), any()))
            .thenReturn(expectedHearingIds);
        List<Long> hearingIds = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "Test", startFrom, hearingStatus);
        assertNotNull(hearingIds.size());
        assertEquals(1, hearingIds.size());
        assertTrue(expectedHearingIds.containsAll(hearingIds));
    }
}
