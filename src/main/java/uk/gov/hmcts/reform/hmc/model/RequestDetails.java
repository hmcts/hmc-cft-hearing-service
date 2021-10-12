package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private LocalDateTime requestTimeStamp;
}
