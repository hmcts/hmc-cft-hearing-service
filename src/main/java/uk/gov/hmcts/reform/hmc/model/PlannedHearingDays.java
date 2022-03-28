package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
@Setter
@Getter
public class PlannedHearingDays {

    private LocalDate plannedStartTime;
    private LocalDate plannedEndTime;
    private List<PartyDetails> plannedHearingDays;
}
