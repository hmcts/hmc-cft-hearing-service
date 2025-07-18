package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class InvalidServiceTokenException extends ApiException {

    public InvalidServiceTokenException(final String message) {
        this(message, null);
    }

    public InvalidServiceTokenException(String message, Throwable e) {
        super(message, e);
    }
}
