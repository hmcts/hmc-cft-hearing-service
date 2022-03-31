package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = DayOfWeekUnavailable.class, fieldName = "dow")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    private String dowUnavailabilityType;

}
