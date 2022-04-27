package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ActualHearingDayPartyDetail implements Serializable {

    @NotEmpty(message = ValidationError.HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_NOT_EMPTY)
    @Size(max = 100, message = ValidationError.HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_MAX_LENGTH)
    private String firstName;

    @NotEmpty(message = ValidationError.HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_NOT_EMPTY)
    @Size(max = 100, message = ValidationError.HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_MAX_LENGTH)
    private String lastName;
}
