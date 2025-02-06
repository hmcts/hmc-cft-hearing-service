package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class HmcHearingResponse {
    @NotEmpty(message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{4}$", message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    private String hmctsServiceCode;

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @Pattern(regexp = "^.+$", message = ValidationError.CASE_REF_INVALID)
    private String caseRef;

    @NotEmpty(message = ValidationError.HEARING_ID_NOT_FOUND)
    @Pattern(regexp = "^.+$", message = ValidationError.HEARING_ID_NOT_FOUND)
    private String hearingID;

    @Valid
    private HmcHearingUpdate hearingUpdate;
}
