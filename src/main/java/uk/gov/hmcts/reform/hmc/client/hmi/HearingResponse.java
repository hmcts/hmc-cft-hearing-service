package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@NoArgsConstructor
public class HearingResponse {

    @Valid
    private MetaResponse meta;

    @Valid
    private Hearing hearing;

}
