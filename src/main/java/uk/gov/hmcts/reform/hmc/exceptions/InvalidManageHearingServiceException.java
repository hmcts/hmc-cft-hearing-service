package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class InvalidManageHearingServiceException extends RuntimeException {

    public InvalidManageHearingServiceException(final String message) {
        super(message);
    }

}
