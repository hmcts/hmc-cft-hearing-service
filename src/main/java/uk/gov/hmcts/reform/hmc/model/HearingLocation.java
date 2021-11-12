package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class HearingLocation {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    @JsonProperty("locationType")
    private String locationType;

    @EnumPattern(enumClass = LocationId.class, fieldName = "locationId")
    @JsonProperty("locationId")
    private String locationId;

}
