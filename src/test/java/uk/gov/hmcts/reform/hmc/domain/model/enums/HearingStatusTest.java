package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HearingStatusTest {
    @Test
    void testIsFinalStatus() {
        assertTrue(HearingStatus.isFinalStatus(HearingStatus.COMPLETED));
        assertTrue(HearingStatus.isFinalStatus(HearingStatus.ADJOURNED));
        assertTrue(HearingStatus.isFinalStatus(HearingStatus.CANCELLED));
        assertFalse(HearingStatus.isFinalStatus(HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.isFinalStatus(HearingStatus.AWAITING_LISTING));
        assertFalse(HearingStatus.isFinalStatus(HearingStatus.UPDATE_SUBMITTED));
    }

    @Test
    void testIsGoodStatus() {
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.COMPLETED));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.ADJOURNED));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.CANCELLED));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.AWAITING_LISTING));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.UPDATE_SUBMITTED));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.CANCELLATION_SUBMITTED));
        assertTrue(HearingStatus.isGoodStatus(HearingStatus.LISTED));
        assertFalse(HearingStatus.isGoodStatus(HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.isGoodStatus(HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.isGoodStatus(HearingStatus.EXCEPTION));
    }

    @Test
    void testShouldUpdateLastGoodStatus() {
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.AWAITING_LISTING));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.UPDATE_SUBMITTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.CANCELLATION_SUBMITTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.LISTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.ADJOURNED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(null, HearingStatus.EXCEPTION));

        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING, HearingStatus.LISTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING,
                                                                HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING,
                                                                HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.AWAITING_LISTING,
                                                                HearingStatus.EXCEPTION));

        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED, HearingStatus.LISTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED,
                                                                HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED,
                                                                HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.UPDATE_SUBMITTED,
                                                                HearingStatus.EXCEPTION));

        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.COMPLETED,
                                                                HearingStatus.CANCELLED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.ADJOURNED,
                                                                HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodStatus(HearingStatus.CANCELLED,
                                                                HearingStatus.CANCELLATION_SUBMITTED));

    }
}

