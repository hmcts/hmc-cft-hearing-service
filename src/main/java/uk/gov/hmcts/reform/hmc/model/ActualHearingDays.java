package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
@Setter
@Getter
public class ActualHearingDays {

    private final String hearingDate;
    private final LocalDate hearingStartTime;
    private final LocalDate hearingEndTime;
    private final PauseDateTimes pauseDateTimes;
    private final List<ActualDayParty> actualDayParties;
}
