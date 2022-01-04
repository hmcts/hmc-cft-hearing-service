package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HmiSubmitHearingRequest {

    @JsonProperty("hearingRequest")
    private HmiHearingRequest hearingRequest;

}
