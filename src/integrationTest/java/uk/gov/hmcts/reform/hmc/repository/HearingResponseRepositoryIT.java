package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;

class HearingResponseRepositoryIT extends BaseTest {

    @Autowired
    HearingResponseRepository hearingResponseRepository;

    private static final String INSERT_CASE_HEARING_DATA_SCRIPT = "classpath:sql/insert-case_hearing_request.sql";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_CASE_HEARING_DATA_SCRIPT})
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        String dateStr = "2019-12-10 11:00:00";
        LocalDateTime startFrom = convertDateTime(dateStr);
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        Page<Long> expected = hearingResponseRepository.
            getUnNotifiedHearingsWithOutStartDateTo("ABA1",startFrom, limit);
        assertNotNull( expected.getContent());
        assertEquals(2,expected.getTotalElements());
    }

    private LocalDateTime convertDateTime(String dateStr){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateStr,format);
    }
}
