package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FutureHearingErrorDetails {

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;
}
