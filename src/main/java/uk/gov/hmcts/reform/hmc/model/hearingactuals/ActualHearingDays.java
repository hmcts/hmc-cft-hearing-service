package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class ActualHearingDays {

    private LocalDate hearingDate;
    private LocalDateTime hearingStartTime;
    private LocalDateTime hearingEndTime;
    private List<PauseDateTimes> pauseDateTimes;
    private List<ActualDayParty> actualDayParties;
    private Boolean notRequired;
}
