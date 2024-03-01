package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.EnumSet;

public enum HearingStatus {
    HEARING_REQUESTED,
    AWAITING_LISTING,
    UPDATE_REQUESTED,
    UPDATE_SUBMITTED,
    ADJOURNED,
    COMPLETED,
    LISTED,
    CANCELLATION_REQUESTED,
    CANCELLATION_SUBMITTED,
    CANCELLED,
    CLOSED,
    EXCEPTION;

    private static final EnumSet<HearingStatus> FINAL_STATES = EnumSet.of(COMPLETED, ADJOURNED, CANCELLED);
    private static final EnumSet<HearingStatus> GOOD_STATES = EnumSet.of(COMPLETED, ADJOURNED, CANCELLED, AWAITING_LISTING, UPDATE_SUBMITTED, CANCELLATION_SUBMITTED, LISTED);

    public static boolean isFinalState(HearingStatus status) {
        return FINAL_STATES.contains(status);
    }

    public static boolean isGoodState(HearingStatus status) {
        return GOOD_STATES.contains(status);
    }

    public static boolean shouldUpdateLastGoodState(HearingStatus lastGoodState, HearingStatus currentStatus) {
        if (lastGoodState == null) {
            return isGoodState(currentStatus);
        } else {
            return !isFinalState(lastGoodState)
                && isGoodState(currentStatus);
        }
    }
}

