package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HearingResultTypeTest {

    @Mock
    private HearingResultType hearingResultType;

    @Test
    @DisplayName("should get result type by label")
    void shouldWork(){

        HearingResultType resultType = hearingResultType.getByLabel("COMPLETED");

        assertThat(resultType, is(HearingResultType.COMPLETED));
    }

}
