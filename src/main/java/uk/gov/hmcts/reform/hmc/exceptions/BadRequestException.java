package uk.gov.hmcts.reform.hmc.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(final String message) {
        super(message);
    }
}
