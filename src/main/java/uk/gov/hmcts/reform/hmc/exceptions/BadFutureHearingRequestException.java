package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;

@Getter
public class BadFutureHearingRequestException extends RuntimeException {

    private final Integer statusCode;
    private final String errorResponse;

    public BadFutureHearingRequestException(String message) {
        super(message);
        this.statusCode = null;
        this.errorResponse = null;
    }

    public BadFutureHearingRequestException(String message, Integer statusCode, String errorResponse) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }
}
