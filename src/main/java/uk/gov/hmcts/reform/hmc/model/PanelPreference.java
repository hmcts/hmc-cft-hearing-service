package uk.gov.hmcts.reform.hmc.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class PanelPreference {

    @NotEmpty(message = ValidationError.MEMBER_ID_EMPTY)
    @Size(max = 70, message = ValidationError.MEMBER_ID_MAX_LENGTH)
    private String memberID;

    @Size(max = 70, message = ValidationError.MEMBER_TYPE_MAX_LENGTH)
    private String memberType;

    @EnumPattern(enumClass = RequirementType.class, fieldName = "requirementType")
    @Schema(allowableValues = "MUSTINC, OPTINC, EXCLUDE")
    private String requirementType;
}
