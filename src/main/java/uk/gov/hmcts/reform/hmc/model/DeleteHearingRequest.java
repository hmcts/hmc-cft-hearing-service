package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CANCELLATION_REASON_CODE_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteHearingRequest {

    @NotEmpty(message = ValidationError.INVALID_CANCELLATION_REASON_CODE)
    @Size(max = 100, message = CANCELLATION_REASON_CODE_MAX_LENGTH_MSG)
    private String cancellationReasonCode;

    @NotNull(message = ValidationError.VERSION_NUMBER_NULL_EMPTY)
    private Integer versionNumber;
}
