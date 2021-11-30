package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = Dow.class, fieldName = "dow")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    @EnumPattern(enumClass = DowUnavailabilityType.class, fieldName = "dowUnavailabilityType")
    private String dowUnavailabilityType;

}
