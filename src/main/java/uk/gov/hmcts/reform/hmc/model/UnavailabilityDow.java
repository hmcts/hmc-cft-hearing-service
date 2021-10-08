package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UnavailabilityDow {

    @JsonProperty("Dow")
    private Dow dow;

    @JsonProperty("DOWUnavailabilityType")
    private DowUnavailabilityType dowUnavailabilityType;

}
