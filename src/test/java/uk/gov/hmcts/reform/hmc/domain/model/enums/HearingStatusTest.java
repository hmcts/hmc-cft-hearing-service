package uk.gov.hmcts.reform.hmc.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HearingStatusTest {
    @Test
    void testIsFinalStatus() {
        assertTrue(HearingStatus.isFinalState(HearingStatus.COMPLETED));
        assertTrue(HearingStatus.isFinalState(HearingStatus.ADJOURNED));
        assertTrue(HearingStatus.isFinalState(HearingStatus.CANCELLED));
        assertFalse(HearingStatus.isFinalState(HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.isFinalState(HearingStatus.AWAITING_LISTING));
        assertFalse(HearingStatus.isFinalState(HearingStatus.UPDATE_SUBMITTED));
    }

    @Test
    void testIsGoodStatus() {
        assertTrue(HearingStatus.isGoodState(HearingStatus.COMPLETED));
        assertTrue(HearingStatus.isGoodState(HearingStatus.ADJOURNED));
        assertTrue(HearingStatus.isGoodState(HearingStatus.CANCELLED));
        assertTrue(HearingStatus.isGoodState(HearingStatus.AWAITING_LISTING));
        assertTrue(HearingStatus.isGoodState(HearingStatus.UPDATE_SUBMITTED));
        assertTrue(HearingStatus.isGoodState(HearingStatus.CANCELLATION_SUBMITTED));
        assertTrue(HearingStatus.isGoodState(HearingStatus.LISTED));
        assertFalse(HearingStatus.isGoodState(HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.isGoodState(HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.isGoodState(HearingStatus.EXCEPTION));
    }

    @Test
    void testShouldUpdateLastGoodStatus() {
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.AWAITING_LISTING));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.UPDATE_SUBMITTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.CANCELLATION_SUBMITTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.LISTED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.ADJOURNED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(null, HearingStatus.EXCEPTION));

        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.LISTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.AWAITING_LISTING, HearingStatus.EXCEPTION));

        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.CANCELLED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.COMPLETED));
        assertTrue(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.LISTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.HEARING_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.UPDATE_SUBMITTED, HearingStatus.EXCEPTION));

        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.COMPLETED, HearingStatus.CANCELLED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.ADJOURNED, HearingStatus.UPDATE_REQUESTED));
        assertFalse(HearingStatus.shouldUpdateLastGoodState(HearingStatus.CANCELLED, HearingStatus.CANCELLATION_SUBMITTED));

    }
}

