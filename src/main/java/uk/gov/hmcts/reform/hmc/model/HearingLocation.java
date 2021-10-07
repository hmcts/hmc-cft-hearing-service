package uk.gov.hmcts.reform.hmc.model;

import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;

public class HearingLocation {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    private String locationType;

    private LocationId locationId;
}
