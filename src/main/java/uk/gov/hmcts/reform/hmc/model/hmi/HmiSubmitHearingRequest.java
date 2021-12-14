package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HmiSubmitHearingRequest {

    @JsonProperty("hearingRequest")
    private HmiHearingRequest hearingRequest;

}
