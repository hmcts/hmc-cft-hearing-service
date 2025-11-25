package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingResponse {

    @Valid
    private MetaResponse meta;

    @Valid
    private Hearing hearing;

}
