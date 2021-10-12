package uk.gov.hmcts.reform.hmc.model;

import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class CaseCategory {

    @EnumPattern(enumClass = CategoryType.class, fieldName = "CategoryType")
    private String categoryType;

    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;
}
