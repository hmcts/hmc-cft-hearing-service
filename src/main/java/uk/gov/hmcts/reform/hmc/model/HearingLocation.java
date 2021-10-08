package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class HearingLocation {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    private String locationType;

    @NotNull
    private LocationId locationId;

}
