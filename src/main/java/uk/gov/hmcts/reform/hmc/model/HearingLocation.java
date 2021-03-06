package uk.gov.hmcts.reform.hmc.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class HearingLocation {

    @EnumPattern(enumClass = LocationType.class, fieldName = "locationType")
    @ApiModelProperty(allowableValues = "court, cluster, region")
    private String locationType;

    @NotEmpty(message = ValidationError.LOCATION_ID_EMPTY)
    private String locationId;

}
