package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.CapitalizedEnumPattern;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @JsonProperty("DOW")
    @CapitalizedEnumPattern(enumClass = DayOfWeekUnavailable.class, fieldName = "dow")
    @Schema(allowableValues = "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    @Schema(allowableValues = "AM, PM, All Day")
    private String dowUnavailabilityType;

}
