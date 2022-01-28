package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private LocalDateTime requestTimeStamp;

    @NotNull
    private String status;

    @NotNull
    private int versionNumber;

    private LocalDateTime partiesNotified;


}
