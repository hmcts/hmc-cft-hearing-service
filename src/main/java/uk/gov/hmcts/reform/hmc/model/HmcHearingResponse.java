package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
public class HmcHearingResponse {
    @NotEmpty(message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{4}$", message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    private String hmctsServiceCode;

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @Pattern(regexp = "^\\d{16}$", message = ValidationError.CASE_REF_INVALID)
    private String caseRef;

    private String hearingID;

    @Valid
    private HmcHearingUpdate hearingUpdate;
}
