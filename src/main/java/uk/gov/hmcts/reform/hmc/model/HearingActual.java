package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class HearingActual {
    private final HearingOutcome hearingOutcome;
    private final List<ActualHearingDays> actualHearingDays;
}
