package uk.gov.hmcts.reform.hmc.client.hmi;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingCodeTest {

    private HearingCode hearingResultType;

    @Test
    void shouldVerifyValidHearingCode() {
        assertTrue(hearingResultType.isValid("LISTED"));
    }

    @Test
    void shouldVerifyWhenHearingCodeIsInValid() {
        assertFalse(hearingResultType.isValid("random"));
    }
}
