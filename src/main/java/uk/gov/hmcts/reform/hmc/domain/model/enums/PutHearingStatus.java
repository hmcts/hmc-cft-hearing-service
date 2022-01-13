package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;

public enum PutHearingStatus {
    HEARING_REQUESTED,
    UPDATE_REQUESTED,
    UPDATE_SUBMITTED,
    AWAITING_LISTING,
    LISTED;

    public static boolean isValid(String status) {
        return Arrays.stream(values()).anyMatch(enumStatus -> enumStatus.name().equals(status));
    }
}

