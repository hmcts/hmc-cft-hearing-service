package uk.gov.hmcts.reform.hmc.domain.model.enums;

public enum DeleteHearingStatus {
    HEARING_REQUESTED,
    UPDATE_REQUESTED,
    UPDATE_SUBMITTED,
    AWAITING_LISTING,
    LISTED;

    public static boolean chekStatus(String status) {
        for (DeleteHearingStatus enumStatus : values()) {
            if (enumStatus.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}


