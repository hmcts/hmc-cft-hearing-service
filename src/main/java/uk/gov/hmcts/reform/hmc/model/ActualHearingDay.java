package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ActualHearingDay implements Serializable {

    @NotNull(message = ValidationError.HA_HEARING_DAY_HEARING_DATE_NOT_EMPTY)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime hearingStartTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime hearingEndTime;

    @Valid
    private List<ActualHearingDayPauseDayTime> pauseDateTimes;

    @Valid
    private List<ActualHearingDayParties> actualDayParties;

    private Boolean notRequired;

    public boolean isEmpty() {
        return hearingStartTime == null
            && hearingEndTime == null
            && (pauseDateTimes == null || pauseDateTimes.isEmpty())
            && (actualDayParties == null || actualDayParties.isEmpty());
    }

}
