package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Getter
public class HearingOutcome {
    private final String hearingType;
    private final Boolean hearingFinalFlag;
    private final Object hearingResult;
    private final String hearingResultReasonType;
    private final LocalDate hearingResultDate;
}
