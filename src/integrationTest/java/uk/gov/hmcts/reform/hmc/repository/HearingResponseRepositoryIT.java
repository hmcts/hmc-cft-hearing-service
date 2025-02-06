package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertDateTime;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HearingResponseRepositoryIT extends BaseTest {

    @Autowired
    UnNotifiedHearingsRepository unNotifiedHearingsRepository;

    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    List<String> hearingStatusListed  = List.of("LISTED");
    List<String> hearingStatusCancelled  = List.of("CANCELLED");

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        final List<Long> expected = Arrays.asList(2100000000L, 2100000001L);
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<Long> hearingIds = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "ACA2",
            startFrom,
            hearingStatusListed
        );
        assertNotNull(hearingIds.size());
        assertThat(2).isEqualTo(hearingIds.size());
        assertTrue(expected.containsAll(hearingIds));
        assertThat(hearingIds).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithStartDateTo() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000000L, 2100000001L);
        String dateStrFrom = "2019-12-10 11:00:00";
        String dateStrTo = "2021-10-01 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
            "ACA2",
            startFrom,
            startTo,
            hearingStatusListed
        );
        assertNotNull(expected.size());
        assertEquals(2, expected.size());
        assertEquals(expectedHearingIds, expected);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWhenPartiesDateTimeIsNull() {
        String dateStrFrom = "2010-02-01 12:00:00";
        String dateStrTo = "2011-12-12 12:00:00";
        final List<Long> expectedHearingIds = Arrays.asList(2100000004L);
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
            "AAA2",
            startFrom,
            startTo,
            hearingStatusListed
        );
        assertNotNull(expected.size());
        assertEquals(1, expected.size());
        assertEquals(expectedHearingIds, expected);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsStartDateTimeIsGreater() {
        String dateStr = "2025-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "ACA2",
            startFrom,
            hearingStatusListed
        );
        assertThat(expected.isEmpty());
        assertEquals(0, expected.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsEndDateTimeIsLesser() {
        String dateStr = "2025-12-10 11:00:00";
        String dateStrTo = "2025-12-12 12:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
            "ACA2",
            startFrom,
            startTo,
            hearingStatusListed
        );
        assertThat(expected.isEmpty());
        assertEquals(0, expected.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsReturnUniqueHearingsWithOutStartDateTo() {
        String dateStr = "2023-06-15 01:01:01";
        LocalDateTime startFrom = convertDateTime(dateStr);
        final List<Long> expectedHearingIds = Arrays.asList(2100000003L);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "AAA2",
            startFrom,
            hearingStatusListed
        );
        assertNotNull(expected.isEmpty());
        assertEquals(1, expected.size());
        assertEquals(expectedHearingIds, expected);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsReturnUniqueHearingsWithStartDateTo() {
        String dateStr = "2023-06-15 01:01:01";
        String dateStrTo = "2023-06-20 01:01:01";
        LocalDateTime startFrom = convertDateTime(dateStr);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        final List<Long> expectedHearingIds = Arrays.asList(2100000003L);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
            "AAA2",
            startFrom,
            startTo,
            hearingStatusListed
        );
        assertNotNull(expected.isEmpty());
        assertEquals(1, expected.size());
        assertEquals(expectedHearingIds, expected);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithOutStartDateToForCancelled() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000005L);
        String dateStr = "2021-08-10 08:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "AAA2",
            startFrom,
            hearingStatusCancelled
        );
        assertNotNull(expected.size());
        assertEquals(1, expected.size());
        assertTrue(expectedHearingIds.containsAll(expected));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithOutStartDateToForMultiStatus() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000005L);
        String dateStr = "2023-05-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        List<Long> expected = unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "AAA2",
            startFrom,
            hearingStatusCancelled
        );
        assertNotNull(expected.size());
        assertEquals(1, expected.size());
        assertTrue(expectedHearingIds.containsAll(expected));
    }

}
