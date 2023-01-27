package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FhBadRequestException extends RuntimeException {
    public FhBadRequestException(final String message) {
        super(message);
    }
}
