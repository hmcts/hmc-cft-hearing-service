package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ActualHearingDay {

    private LocalDate hearingDate;
    private LocalDateTime hearingStartTime;
    private LocalDateTime hearingEndTime;
    private List<ActualHearingDayPauseDayTime> pauseDateTimes;
    private List<ActualHearingDayParties> actualDayParties;

}
