package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingJoh {

    private String johId;
    @Size(max = 30, message = ValidationError.HEARING_JOH_CODE_LENGTH)
    private String johCode;
    private String johName;
    private Object johPosition;
    private boolean isPresiding;
}
