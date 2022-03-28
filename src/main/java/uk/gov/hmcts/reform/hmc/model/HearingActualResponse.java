package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Data
@Setter
@Getter
public class HearingActualResponse {
    private String hmcStatus;
    private CaseDetails caseDetails;
    private HearingPlanned hearingPlanned;
    private List<HearingActual> hearingActuals;
}
