package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class MetaResponse {

    @NotNull(message = ValidationError.TRANSACTION_ID_CASE_HQ_EMPTY)
    private LocalDateTime timestamp;

    @NotEmpty(message = ValidationError.TRANSACTION_ID_CASE_HQ_NULL)
    @Size(max = 60, message = ValidationError.TRANSACTION_ID_CASE_HQ_LENGTH)
    private String transactionIdCaseHQ;
}
