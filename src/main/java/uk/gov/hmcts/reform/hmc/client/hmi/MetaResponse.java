package uk.gov.hmcts.reform.hmc.client.hmi;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MetaResponse {

    @NotNull(message = ValidationError.TRANSACTION_ID_CASE_HQ_EMPTY)
    private LocalDateTime timestamp;

    @NotEmpty(message = ValidationError.TRANSACTION_ID_CASE_HQ_NULL)
    @Size(max = 60, message = ValidationError.TRANSACTION_ID_CASE_HQ_LENGTH)
    private String transactionIdCaseHQ;
}
