package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class HearingPlanned {

    private String plannedHearingType;
    private List<PlannedHearingDays> plannedHearingDays;
}
