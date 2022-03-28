package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
public class PlannedHearingDays {

    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private List<PartyDetails> parties;
}
