package uk.gov.hmcts.reform.hmc.exceptions;

public class ApiException extends RuntimeException {

    public ApiException(final String message, final Throwable e) {
        super(message, e);
    }
}
