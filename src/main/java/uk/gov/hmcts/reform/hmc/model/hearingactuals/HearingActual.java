package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class HearingActual {
    private HearingOutcome hearingOutcome;
    private List<ActualHearingDays> actualHearingDays;
}
