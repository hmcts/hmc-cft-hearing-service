package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HearingRepositoryIT extends BaseTest {

    @Autowired
    HearingRepository hearingRepository;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetStatus_HearingIdIsValid() {
        String status = hearingRepository.getStatus(2000000000L);
        assertEquals("HEARING_REQUESTED", status);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testFindByHearingId() {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(2000000000L);
        assertEquals(Long.valueOf(2000000000), hearingResult.get().getId());
        assertNotNull(hearingResult.get().getStatus());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testFindById_HearingIdNotPresentInDb() {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(2200000000L);
        assertTrue(hearingResult.isEmpty());
    }

}
