package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;

@Getter
public abstract class HealthCheckException extends RuntimeException {

    private final Integer statusCode;
    private final String errorResponse;

    protected HealthCheckException(String message) {
        super(message);
        this.statusCode = null;
        this.errorResponse = null;
    }

    protected HealthCheckException(String message, Integer statusCode, String errorResponse) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public abstract String getApiName();
}
