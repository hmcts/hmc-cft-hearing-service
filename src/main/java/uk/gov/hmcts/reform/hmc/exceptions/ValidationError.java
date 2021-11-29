package uk.gov.hmcts.reform.hmc.exceptions;

public final class ValidationError {

    private ValidationError() {
    }

    public static final String INVALID_CANCELLATION_REASON_CODE = "Cancellation Reason code details are not present";
    public static final String CANCELLATION_REASON_CODE_MAX_LENGTH_MSG = "Non standard hearing duration "
        + "reasons length cannot be greater than 100 characters";
    public static final String INVALID_VERSION_NUMBER = "Invalid Version number";
    public static final String INVALID_HEARING_ID_DETAILS = "Invalid hearing Id";
}
