package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CaseCategory {

    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    @JsonProperty("categoryType")
    private String categoryType;

    @JsonProperty("categoryValue")
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;
}
