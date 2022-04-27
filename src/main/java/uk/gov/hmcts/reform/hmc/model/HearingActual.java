package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingActual implements Serializable {

    @Valid
    @JsonProperty("hearingOutcome")
    private HearingActualsOutcome hearingOutcome;

    @Valid
    @JsonProperty("actualHearingDays")
    private List<ActualHearingDay> actualHearingDays;

    public List<ActualHearingDay> getActualHearingDays() {
        return actualHearingDays == null ? List.of() : actualHearingDays;
    }
}
