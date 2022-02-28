package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HearingNotFoundException extends RuntimeException {

    public HearingNotFoundException(Long hearingId, String errorMessage) {
        super(String.format(errorMessage, hearingId));
    }
}
