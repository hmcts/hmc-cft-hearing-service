package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorDetails {

    @JsonProperty("errCode")
    private Integer errorCode;

    @JsonProperty("errDesc")
    private String errorDescription;
}
