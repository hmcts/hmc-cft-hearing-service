package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumNamePattern;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class PanelPreference {

    @NotEmpty(message = ValidationError.MEMBER_ID_EMPTY)
    @Size(max = 70, message = ValidationError.MEMBER_ID_MAX_LENGTH)
    private String memberID;

    @Size(max = 70, message = ValidationError.MEMBER_TYPE_MAX_LENGTH)
    private String memberType;

    @EnumNamePattern(regexp = "MUSTINC|OPTINC|EXCLUDE")
    private RequirementType requirementType;
}
