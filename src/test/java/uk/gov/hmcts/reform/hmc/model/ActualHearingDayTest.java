package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActualHearingDayTest {

    private ActualHearingDay actualHearingDay;

    @BeforeEach
    void setUp() {
        actualHearingDay = new ActualHearingDay();
    }

    @Test
    void notRequiredDefaultToFalse() {
        assertFalse(actualHearingDay.getNotRequired(), "Default notRequired value should be false");
    }

    @Test
    void notRequiredFalseWhenValueNull() {
        actualHearingDay.setNotRequired(null);
        assertFalse(actualHearingDay.getNotRequired(), "notRequired value should be false when set to null");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void notRequiredSetWhenValueNotNull(boolean notRequired) {
        actualHearingDay.setNotRequired(notRequired);

        if (notRequired) {
            assertTrue(actualHearingDay.getNotRequired(), "notRequired value should be true");
        } else {
            assertFalse(actualHearingDay.getNotRequired(), "notRequired value should be false");
        }
    }
}
