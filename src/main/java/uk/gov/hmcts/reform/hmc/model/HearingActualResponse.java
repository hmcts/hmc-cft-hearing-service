package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class HearingActualResponse {
    private final String hmcStatus;
    private final CaseDetails caseDetails;
    private final HearingPlanned hearingPlanned;
    private final HearingActual hearingActual;
}
