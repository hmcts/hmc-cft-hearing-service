package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnNotifiedHearingServiceIT extends BaseTest {

    @Autowired
    private UnNotifiedHearingService unNotifiedHearingService;

    @MockBean
    private ApplicationParams applicationParams;

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithAllMandatoryFields() {
        LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ABA1",startFrom, null);
        assertEquals(1, response.getHearingIds().size());
        assertEquals(2000000000L, response.getHearingIds().get(0));
        assertEquals(3, response.getTotalFound());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void testValidateUnNotifiedHearing_WithStartDateTo() {
        LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);
        LocalDateTime startTo = LocalDateTime.of(2023, 1, 10, 11, 20, 00);
        UnNotifiedHearingsResponse response = unNotifiedHearingService
            .getUnNotifiedHearings("ABA1",startFrom, startTo);
        assertEquals(1, response.getHearingIds().size());
        assertEquals(2000000000L, response.getHearingIds().get(0));
        assertEquals(3, response.getTotalFound());
    }
}
