package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PanelRequirements {

    // @HearingDurationReasonMaxLengthConstraint
    private String[] roleType;

    // @HearingDurationReasonMaxLengthConstraint
    private String[] authorisationTypes;

    //@HearingDurationReasonMaxLengthConstraint
    private String[] authorisationSubType;

    private PanelPreference[] panelPreferences;

    //@HearingDurationReasonMaxLengthConstraint
    private String[] panelSpecialisms;
}
