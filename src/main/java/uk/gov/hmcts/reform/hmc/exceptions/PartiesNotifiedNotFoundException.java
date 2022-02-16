package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PartiesNotifiedNotFoundException extends RuntimeException {

    public PartiesNotifiedNotFoundException(String errorMessage, Long hearingId) {
        super(String.format(errorMessage, hearingId));
    }
}
