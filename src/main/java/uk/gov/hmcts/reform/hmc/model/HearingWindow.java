package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HearingWindow {

    private LocalDate hearingWindowStartDateRange;

    private LocalDate hearingWindowEndDateRange;

    private LocalDateTime firstDateTimeMustBe;
}
