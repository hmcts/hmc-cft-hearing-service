package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ActualHearingOrganisationDetail implements Serializable {

    @NotEmpty(message = ValidationError.HA_HEARING_DAY_ORGANISATION_NAME_NOT_EMPTY)
    @Size(max = 200, message = ValidationError.HA_HEARING_DAY_ORGANISATION_NAME_MAX_LENGTH)
    private String name;
}
