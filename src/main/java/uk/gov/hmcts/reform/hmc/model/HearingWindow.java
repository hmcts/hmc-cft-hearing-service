package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HearingWindow {

    @JsonProperty("hearingWindowStartDateRange")
    private LocalDate hearingWindowStartDateRange;

    @JsonProperty("hearingWindowEndDateRange")
    private LocalDate hearingWindowEndDateRange;

    @JsonProperty("firstDateTimeMustBe")
    private LocalDateTime firstDateTimeMustBe;
}
