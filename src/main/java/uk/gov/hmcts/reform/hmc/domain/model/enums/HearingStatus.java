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

    private static final EnumSet<HearingStatus> FINAL_STATUSES = EnumSet.of(COMPLETED, ADJOURNED, CANCELLED);
    private static final EnumSet<HearingStatus> GOOD_STATUSES = EnumSet.of(COMPLETED, ADJOURNED, CANCELLED, 
        AWAITING_LISTING, UPDATE_SUBMITTED, CANCELLATION_SUBMITTED, LISTED);

    public static boolean isFinalStatus(HearingStatus status) {
        return FINAL_STATUSES.contains(status);
    }

    public static boolean isGoodStatus(HearingStatus status) {
        return GOOD_STATUSES.contains(status);
    }

    public static boolean shouldUpdateLastGoodStatus(HearingStatus lastGoodState, HearingStatus currentStatus) {
        if (lastGoodState == null) {
            return isGoodStatus(currentStatus);
        } else {
            return !isFinalStatus(lastGoodState)
                && isGoodStatus(currentStatus);
        }
    }
}