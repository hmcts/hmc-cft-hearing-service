package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HearingWindow {

    private String hearingWindowStartDateRange;

    private String hearingWindowEndDateRange;

    private String firstDateTimeMustBe;
}
