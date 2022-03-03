package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Getter
public class PlannedHearingDays {

    private LocalDate plannedStartTime;
    private LocalDate plannedEndTime;


}
