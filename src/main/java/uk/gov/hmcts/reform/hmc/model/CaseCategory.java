package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CaseCategory {

    @NotEmpty(message = ValidationError.CATEGORY_TYPE_EMPTY)
    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    private String categoryType;

    @NotEmpty(message = ValidationError.CATEGORY_VALUE_EMPTY)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryParent;
}
