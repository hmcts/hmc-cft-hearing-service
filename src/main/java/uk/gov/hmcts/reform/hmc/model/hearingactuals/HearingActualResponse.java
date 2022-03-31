package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;

@NoArgsConstructor
@Data
@Setter
@Getter
public class HearingActualResponse {
    private String hmcStatus;
    private CaseDetails caseDetails;
    private HearingPlanned hearingPlanned;
    private HearingActual hearingActuals;
}
