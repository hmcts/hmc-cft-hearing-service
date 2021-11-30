package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CaseHearingRequestRepositoryIT extends BaseTest {

    @Autowired
    CaseHearingRequestRepository caseHearingRequestRepository;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testGetVersionNumber_HearingIdIsValid() {
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(2000000000L);
        assertEquals(1, versionNumber);
    }

    @Test
    @Sql(INSERT_CASE_HEARING_DATA_SCRIPT)
    void testGetVersionNumber_HearingIdIsInValid() {
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(2020000001L);
        assertNull(versionNumber);
    }
}
