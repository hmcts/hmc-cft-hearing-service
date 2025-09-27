package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;

@Getter
public abstract class HealthCheckException extends RuntimeException {

    private final Integer statusCode;
    private final String errorMessage;

    protected HealthCheckException(String message) {
        super(message);
        this.statusCode = null;
        this.errorMessage = null;
    }

    protected HealthCheckException(String message, Integer statusCode, String errorMessage) {
        super(message);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public abstract String getApiName();
}
