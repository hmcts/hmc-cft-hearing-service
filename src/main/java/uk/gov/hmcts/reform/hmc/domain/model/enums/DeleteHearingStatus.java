package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;

public enum DeleteHearingStatus {
    HEARING_REQUESTED,
    UPDATE_REQUESTED,
    UPDATE_SUBMITTED,
    AWAITING_LISTING,
    LISTED;

    public static boolean isValid(String status) {
        return Arrays.stream(values()).anyMatch(enumStatus -> enumStatus.name().equals(status));
    }

    public static boolean isValidHearingActuals(DeleteHearingStatus deleteHearingStatus) {
        switch (deleteHearingStatus) {
            case LISTED:
            case UPDATE_REQUESTED:
            case UPDATE_SUBMITTED:
                return true;
            default:
                return false;
        }
    }
}


