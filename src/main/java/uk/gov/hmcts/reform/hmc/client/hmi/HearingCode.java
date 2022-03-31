package uk.gov.hmcts.reform.hmc.client.hmi;

import java.util.Arrays;

public enum HearingCode {
    LISTED,
    PENDING_RELISTING,
    EXCEPTION,
    CLOSED;

    public static boolean isValid(String status) {
        return Arrays.stream(values()).anyMatch(enumStatus -> enumStatus.name().equals(status));
    }
}
