package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HearingsForListOfCasesPaginatedRequest {

    @NotNull(message = ValidationError.PAGE_SIZE_MANDATORY)
    @Positive(message = ValidationError.PAGE_SIZE_POSITIVE)
    private Integer pageSize;

    @NotNull(message = ValidationError.OFFSET_MANDATORY)
    @Min(value = 0, message = ValidationError.OFFSET_MIN_VALUE)
    private Integer offset;

    @Valid
    @NotEmpty(message = ValidationError.CASE_REFERENCES_MANDATORY)
    private List<HearingsForListOfCasesPaginatedRequestCaseReference> caseReferences;
}
