package uk.gov.hmcts.reform.hmc.model;

import java.util.List;

import javax.validation.Valid;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.ListMaxLength;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NON_STANDARD_HEARING_DURATION_REASONS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PANEL_SPECIALISMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ROLE_TYPE;

@Data
@NoArgsConstructor
public class PanelRequirements {

    @ListMaxLength(ListName = ROLE_TYPE)
    private List<String> roleType;

    @ListMaxLength(ListName = AUTHORISATION_TYPE)
    private List<String> authorisationTypes;

    @ListMaxLength(ListName = AUTHORISATION_SUB_TYPE)
    private List<String> authorisationSubType;

    @Valid
    private PanelPreference[] panelPreferences;

    @ListMaxLength(ListName = PANEL_SPECIALISMS)
    private List<String> panelSpecialisms;
}
