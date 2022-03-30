package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
public class PlannedHearingDays {

    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private List<Party> parties;
}
