package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingActual {

    @JsonProperty("hearingOutcome")
    private HearingActualsOutcome hearingOutcome;

    @JsonProperty("actualHearingDays")
    private List<ActualHearingDay> actualHearingDays;

}
