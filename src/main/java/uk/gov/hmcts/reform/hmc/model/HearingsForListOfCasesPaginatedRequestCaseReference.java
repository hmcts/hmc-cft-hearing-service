package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Getter
@Setter
@AllArgsConstructor
public class HearingsForListOfCasesPaginatedRequestCaseReference {

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @Size(min = 16, max = 16, message = ValidationError.CASE_REF_INVALID_LENGTH)
    private String caseReference;
}
