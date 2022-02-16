package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PartiesNotifiedBadRequestException extends RuntimeException {

    public PartiesNotifiedBadRequestException(String errorMessage, Long hearingId) {
        super(String.format(errorMessage, hearingId));
    }
}
