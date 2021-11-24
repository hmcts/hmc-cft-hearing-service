package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @EnumPattern(enumClass = DayOfWeekUnavailable.class, fieldName = "dow")
    private String dow;

    @EnumPattern(enumClass = DayOfWeekUnAvailableType.class, fieldName = "dowUnavailabilityType")
    private String dowUnavailabilityType;

}
