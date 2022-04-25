package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class LinkedGroupNotFoundException extends RuntimeException {

    public LinkedGroupNotFoundException(String requestId, String errorMessage) {
        super(String.format(errorMessage, requestId));
    }
}
