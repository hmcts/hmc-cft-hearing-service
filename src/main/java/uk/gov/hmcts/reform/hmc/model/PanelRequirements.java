package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
public class PanelRequirements {

    private List<@Size(max = 70, message = ROLE_TYPE_MAX_LENGTH_MSG) String> roleType;

    private List<@Size(max = 70, message = AUTHORISATION_TYPE_MAX_LENGTH_MSG) String> authorisationTypes;

    private List<@Size(max = 70, message = AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG) String> authorisationSubType;

    @Valid
    private List<PanelPreference> panelPreferences;

    private List<@Size(max = 70, message = PANEL_SPECIALISMS_MAX_LENGTH_MSG) String> panelSpecialisms;
}
