package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class CaseCategory {

    @NotEmpty(message = ValidationError.CATEGORY_TYPE_EMPTY)
    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    @Schema(allowableValues = "caseType, caseSubType")
    private String categoryType;

    @NotEmpty(message = ValidationError.CATEGORY_VALUE_EMPTY)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryParent;
}
