package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(final String message) {
        this(message, null);
    }

    public ResourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
