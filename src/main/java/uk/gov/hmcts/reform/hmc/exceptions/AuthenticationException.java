package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;

@Getter
public class AuthenticationException extends RuntimeException {

    private final ErrorDetails errorDetails;

    public AuthenticationException(String message, ErrorDetails errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
