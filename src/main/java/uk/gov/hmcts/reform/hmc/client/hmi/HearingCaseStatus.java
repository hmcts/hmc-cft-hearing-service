package uk.gov.hmcts.reform.hmc.client.hmi;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.HearingCodeEnumPattern;

@Data
@NoArgsConstructor
public class HearingCaseStatus {

    @NotNull(message = ValidationError.HEARING_CODE_NULL)
    @HearingCodeEnumPattern(enumClass = HearingCode.class, fieldName = "hearing case status code")
    private String code;
    private String description;
}
