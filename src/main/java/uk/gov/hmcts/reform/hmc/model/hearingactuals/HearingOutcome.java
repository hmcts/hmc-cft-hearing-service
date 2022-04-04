package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Getter
@NoArgsConstructor
public class HearingOutcome {
    private String hearingType;
    private Boolean hearingFinalFlag;
    private Object hearingResult;
    private String hearingResultReasonType;
    private LocalDate hearingResultDate;
}
