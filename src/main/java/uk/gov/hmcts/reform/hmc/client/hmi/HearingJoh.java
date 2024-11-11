package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
public class HearingJoh {

    private String johId;
    @Size(max = 30, message = ValidationError.HEARING_JOH_CODE_LENGTH)
    private String johCode;
    private String johName;
    private JsonNode johPosition;
    private Boolean isPresiding;
}
