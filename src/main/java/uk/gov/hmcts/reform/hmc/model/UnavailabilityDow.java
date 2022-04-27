package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.CapitalizedEnumPattern;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @JsonProperty("DOW")
    @CapitalizedEnumPattern(enumClass = DayOfWeekUnavailable.class, fieldName = "dow")
    @ApiModelProperty(allowableValues = "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    @ApiModelProperty(allowableValues = "AM, PM, All Day")
    private String dowUnavailabilityType;

}
