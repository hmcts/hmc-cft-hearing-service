package uk.gov.hmcts.reform.hmc.model;

import uk.gov.hmcts.reform.hmc.validator.HearingDurationReasonMaxLengthConstraint;

public class PanelRequirements {

    @HearingDurationReasonMaxLengthConstraint
    private String[] roleType;

    @HearingDurationReasonMaxLengthConstraint
    private String[] authorisationTypes;

    @HearingDurationReasonMaxLengthConstraint
    private String[] authorisationSubtype;

    private PanelPreference[] panelPreferences;

    @HearingDurationReasonMaxLengthConstraint
    private String[] panelSpecialism;
}
