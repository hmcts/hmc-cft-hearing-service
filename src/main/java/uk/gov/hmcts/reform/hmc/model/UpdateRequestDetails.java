package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class UpdateRequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private LocalDateTime requestTimeStamp;

    @NotNull(message = ValidationError.INVALID_VERSION_NUMBER)
    private Integer versionNumber;

}
