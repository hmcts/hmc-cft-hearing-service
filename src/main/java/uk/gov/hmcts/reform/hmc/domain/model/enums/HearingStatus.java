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

    public static boolean isFinalStatus(HearingStatus status) {
        return FINAL_STATUSES.contains(status);
    }

    public static boolean shouldUpdateLastGoodStatus(HearingStatus lastGoodStatus, HearingStatus currentStatus) {
        if (lastGoodStatus == null) {
            return EnumSet.of(COMPLETED, ADJOURNED, CANCELLED, AWAITING_LISTING,
                              UPDATE_SUBMITTED, CANCELLATION_SUBMITTED, LISTED)
                .contains(currentStatus);
        } else {
            return EnumSet.of(AWAITING_LISTING, UPDATE_SUBMITTED, CANCELLATION_SUBMITTED, LISTED)
                .contains(lastGoodStatus);
        }
    }
}

