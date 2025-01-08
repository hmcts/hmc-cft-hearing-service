package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetHearingsResponseTest {

    @Test
    void getCaseHearingsReturnsSortedByHearingId() {
        CaseHearing hearing1 = new CaseHearing();
        hearing1.setHearingId(2L);

        CaseHearing hearing2 = new CaseHearing();
        hearing2.setHearingId(1L);

        CaseHearing hearing3 = new CaseHearing();
        hearing3.setHearingId(3L);

        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Arrays.asList(hearing1, hearing2, hearing3));

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(hearing2).isEqualTo(sortedHearings.get(2));
        assertThat(hearing1).isEqualTo(sortedHearings.get(1));
        assertThat(hearing3).isEqualTo(sortedHearings.get(0));
    }

    @Test
    void getCaseHearingsReturnsEmptyListWhenEmpty() {
        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Arrays.asList());

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings).isEmpty();
    }

}
