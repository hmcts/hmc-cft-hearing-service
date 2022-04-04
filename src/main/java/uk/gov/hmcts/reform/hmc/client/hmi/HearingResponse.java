package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingResponse {

    @Valid
    private MetaResponse meta;

    @Valid
    private Hearing hearing;

}
