package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ActualHearingDayPauseDayTime {

    private LocalDateTime pauseStartTime;
    private LocalDateTime pauseEndTime;
}
