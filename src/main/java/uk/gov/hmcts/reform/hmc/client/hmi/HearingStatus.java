package uk.gov.hmcts.reform.hmc.client.hmi;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
public class HearingStatus {

    @NotEmpty(message = ValidationError.HEARING_STATUS_CODE_NULL)
    @Size(max = 30, message = ValidationError.HEARING_STATUS_CODE_LENGTH)
    private String code;
    private String description;
}
