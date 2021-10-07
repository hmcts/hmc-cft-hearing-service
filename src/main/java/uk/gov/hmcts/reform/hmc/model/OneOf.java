package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OneOf {

    private String hearingWindowStartDateRange;

    private String hearingWindowEndDateRange;

    private String firstDateTimeMustBe;
}
