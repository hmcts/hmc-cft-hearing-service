package uk.gov.hmcts.reform.hmc.model;

import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class PanelPreference {

    @NotEmpty(message = ValidationError.MEMBER_ID_EMPTY)
    @Size(max = 70, message = ValidationError.MEMBER_ID_MAX_LENGTH)
    private String memberID;

    @Size(max = 70, message = ValidationError.MEMBER_TYPE_MAX_LENGTH)
    private String memberType;

    private RequirementType requirementType;
}
