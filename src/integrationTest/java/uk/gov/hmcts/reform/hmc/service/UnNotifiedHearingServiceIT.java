package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EXCEPTION_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_STATUS_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.convertDateTime;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UnNotifiedHearingServiceIT extends BaseTest {

    @Autowired
    private UnNotifiedHearingService unNotifiedHearingService;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";

    List<String> hearingStatus  = List.of("LISTED");

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithAllMandatoryFields() {
        String dateStr = "2019-12-10 11:00:00";
        List<String> hearingStatusListed  = List.of("LISTED");
        LocalDateTime startFrom = convertDateTime(dateStr);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ACA2", startFrom, null,hearingStatusListed);
        final List<String> expectedHearingIds = Arrays.asList("2100000001", "2100000000");
        assertNotNull(response.getHearingIds());
        assertEquals(2, response.getHearingIds().size());
        assertEquals(2, response.getTotalFound());
        assertThat(response.getHearingIds()).containsExactlyInAnyOrderElementsOf(expectedHearingIds);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithStartDateTo() {
        String dateStrFrom = "2019-12-10 11:00:00";
        String dateStrTo = "2021-10-01 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ACA2", startFrom, startTo, hearingStatus);
        final List<String> expectedHearingIds = Arrays.asList("2100000000", "2100000001");
        assertNotNull(response.getHearingIds());
        assertEquals(2, response.getHearingIds().size());
        assertEquals(2, response.getTotalFound());
        assertEquals(new HashSet<>(expectedHearingIds), new HashSet<>(response.getHearingIds()));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWhenPartiesDateTimeIsNull() {
        String dateStrFrom = "2010-02-01 12:00:00";
        String dateStrTo = "2011-12-12 12:00:00";
        LocalDateTime startFrom = convertDateTime(dateStrFrom);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("AAA2", startFrom, startTo, hearingStatus);
        final List<String> expectedHearingIds = Arrays.asList("2100000004");
        assertNotNull(response.getHearingIds());
        assertEquals(1, response.getHearingIds().size());
        assertEquals(1, response.getTotalFound());
        assertEquals(expectedHearingIds, response.getHearingIds());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsStartDateTimeIsGreater() {
        String dateStr = "2025-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings(
                "ACA2",
                startFrom, null, hearingStatus
            );
        assertThat(response.getHearingIds().isEmpty());
        assertEquals(0, response.getTotalFound());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testGetUnNotifiedHearingsEndDateTimeIsLesser() {
        String dateStr = "2025-12-10 11:00:00";
        String dateStrTo = "2025-12-12 12:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        LocalDateTime startTo = convertDateTime(dateStrTo);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings(
                "ACA2",
                startFrom, startTo, hearingStatus
            );
        assertThat(response.getHearingIds().isEmpty());
        assertEquals(0, response.getTotalFound());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithHearingStatus_Exception() {
        String dateStr = "2019-12-10 11:00:00";
        List<String> hearingStatusException  = List.of(EXCEPTION_STATUS);
        LocalDateTime startFrom = convertDateTime(dateStr);
        Exception exception = assertThrows(BadRequestException.class, () -> unNotifiedHearingService
            .getUnNotifiedHearings("ACA2", startFrom, null,hearingStatusException));
        assertEquals(HEARING_STATUS_EXCEPTION, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void getUnNotifiedHearing_WithHearingStatus_Cancelled() {
        String dateStr = "2019-12-10 11:00:00";
        final List<String> expectedHearingIds = Arrays.asList("2100000005");
        List<String> hearingStatusCancelled  = List.of(CANCELLED);
        LocalDateTime startFrom = convertDateTime(dateStr);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings(
                "AAA2",
                startFrom, null, hearingStatusCancelled
            );
        assertFalse(response.getHearingIds().isEmpty());
        assertEquals(expectedHearingIds, response.getHearingIds());

    }
}
