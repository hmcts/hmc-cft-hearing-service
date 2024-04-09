package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertDateTime;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HearingResponseRepositoryIT extends BaseTest {

    @Autowired
    HearingResponseRepository hearingResponseRepository;

    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000001L, 2100000000L);
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "ACA2",
            startFrom,
            limit
        );
        assertNotNull(expected.getContent());
        assertEquals(2, expected.getContent().size());
        assertEquals(2, expected.getTotalElements());
        assertEquals(expectedHearingIds, expected.getContent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithStartDateTo() {
        final List<Long> expectedHearingIds = Arrays.asList(2100000001L, 2100000000L);
        String dateStrFrom = "2019-12-10 11:00:00";
        String dateStrTo = "2021-10-01 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
            "ACA2",
            startFrom,
            startTo,
            limit
        );
        assertNotNull(expected.getContent());
        assertNotNull(expected.getContent());
        assertEquals(2, expected.getContent().size());
        assertEquals(2, expected.getTotalElements());
        assertEquals(expectedHearingIds, expected.getContent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWhenPartiesDateTimeIsNull() {
        String dateStrFrom = "2010-02-01 12:00:00";
        String dateStrTo = "2011-12-12 12:00:00";
        final List<Long> expectedHearingIds = Arrays.asList(2100000004L);
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
            "AAA2",
            startFrom,
            startTo,
            limit
        );
        assertNotNull(expected.getContent());
        assertNotNull(expected.getContent());
        assertEquals(1, expected.getContent().size());
        assertEquals(1, expected.getTotalElements());
        assertEquals(expectedHearingIds, expected.getContent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsStartDateTimeIsGreater() {
        String dateStr = "2025-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "ACA2",
            startFrom,
            limit
        );
        assertThat(expected.getContent().isEmpty());
        assertEquals(0, expected.getTotalElements());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsEndDateTimeIsLesser() {
        String dateStr = "2025-12-10 11:00:00";
        String dateStrTo = "2025-12-12 12:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
            "ACA2",
            startFrom,
            startTo,
            limit
        );
        assertThat(expected.getContent().isEmpty());
        assertEquals(0, expected.getTotalElements());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsReturnUniqueHearingsWithOutStartDateTo() {
        String dateStr = "2023-06-15 01:01:01";
        LocalDateTime startFrom = convertDateTime(dateStr);
        final List<Long> expectedHearingIds = Arrays.asList(2100000003L);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo(
            "AAA2",
            startFrom,
            limit
        );
        assertNotNull(expected.getContent());
        assertEquals(1, expected.getTotalElements());
        assertEquals(expectedHearingIds, expected.getContent());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsReturnUniqueHearingsWithStartDateTo() {
        String dateStr = "2023-06-15 01:01:01";
        String dateStrTo = "2023-06-20 01:01:01";
        LocalDateTime startFrom = convertDateTime(dateStr);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        final List<Long> expectedHearingIds = Arrays.asList(2100000003L);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
            "AAA2",
            startFrom,
            startTo,
            limit
        );
        assertNotNull(expected.getContent());
        assertEquals(1, expected.getTotalElements());
        assertEquals(expectedHearingIds, expected.getContent());
    }

}
