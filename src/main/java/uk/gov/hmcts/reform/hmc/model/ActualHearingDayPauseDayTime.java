package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ActualHearingDayPauseDayTime implements Serializable {

    @NotNull(message = ValidationError.HA_HEARING_DAY_PAUSE_START_TIME_NOT_EMPTY)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pauseStartTime;

    @NotNull(message = ValidationError.HA_HEARING_DAY_PAUSE_END_TIME_DATE_NOT_EMPTY)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pauseEndTime;
}
