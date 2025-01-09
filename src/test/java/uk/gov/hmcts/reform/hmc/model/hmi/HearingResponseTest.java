package uk.gov.hmcts.reform.hmc.model.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HearingResponseTest {

    @Test
    void testGetHearingDayScheduleReturnsSortedByHearingStartDateTime() {
        HearingDaySchedule schedule1 = new HearingDaySchedule();
        schedule1.setHearingStartDateTime(LocalDateTime.of(2023, 10, 1, 10, 0));

        HearingDaySchedule schedule2 = new HearingDaySchedule();
        schedule2.setHearingStartDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));

        HearingDaySchedule schedule3 = new HearingDaySchedule();
        schedule3.setHearingStartDateTime(LocalDateTime.of(2023, 10, 1, 11, 0));

        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingDaySchedule(Arrays.asList(schedule1, schedule2, schedule3));

        List<HearingDaySchedule> sortedSchedules = hearingResponse.getHearingDaySchedule();

        assertThat(schedule2).isEqualTo(sortedSchedules.get(0));
        assertThat(schedule1).isEqualTo(sortedSchedules.get(1));
        assertThat(schedule3).isEqualTo(sortedSchedules.get(2));
    }

}
