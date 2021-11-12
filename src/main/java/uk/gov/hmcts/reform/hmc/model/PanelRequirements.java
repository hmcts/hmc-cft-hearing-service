package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
public class PanelRequirements {

    @JsonProperty("roleType")
    private List<@Size(max = 70, message = ROLE_TYPE_MAX_LENGTH_MSG) String> roleType;

    @JsonProperty("authorisationTypes")
    private List<@Size(max = 70, message = AUTHORISATION_TYPE_MAX_LENGTH_MSG) String> authorisationTypes;

    @JsonProperty("authorisationSubType")
    private List<@Size(max = 70, message = AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG) String> authorisationSubType;

    @Valid
    @JsonProperty("panelPreferences")
    private List<PanelPreference> panelPreferences;

    @JsonProperty("panelSpecialisms")
    private List<@Size(max = 70, message = PANEL_SPECIALISMS_MAX_LENGTH_MSG) String> panelSpecialisms;
}
