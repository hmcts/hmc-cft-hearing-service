package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class HearingCaseStatus {

    @NotNull(message = ValidationError.HEARING_CODE_NULL)
    private HearingCode code;
    private String description;
}
