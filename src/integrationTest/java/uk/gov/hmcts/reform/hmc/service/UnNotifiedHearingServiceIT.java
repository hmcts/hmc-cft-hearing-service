package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnNotifiedHearingServiceIT extends BaseTest {

    @Autowired
    private UnNotifiedHearingService unNotifiedHearingService;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String UN_NOTIFIED_HEARINGS_DATA_SCRIPT = "classpath:sql/unNotified-hearings-request.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithAllMandatoryFields() {
        LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 00, 00);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ACA2",startFrom, null);
       /* assertEquals(1, response.getHearingIds().size());
        assertEquals("2000000000", response.getHearingIds().get(0));
        assertEquals(1, response.getTotalFound());*/
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, UN_NOTIFIED_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithStartDateTo() {
        LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 00, 00);
        LocalDateTime startTo = LocalDateTime.of(2022, 1, 10, 11, 00, 00);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ACA2",startFrom, startTo);
        /*assertEquals(0, response.getHearingIds().size());
        assertEquals(0, response.getTotalFound());
        assertEquals(1, response.getHearingIds().size());
        assertEquals("2000000000", response.getHearingIds().get(0));
        assertEquals(1, response.getTotalFound());*/
    }
}
