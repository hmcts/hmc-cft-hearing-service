package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    @JsonProperty("requestTimeStamp")
    private LocalDateTime requestTimeStamp;
}
