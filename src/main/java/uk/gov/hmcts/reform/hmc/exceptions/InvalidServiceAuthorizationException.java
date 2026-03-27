package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidServiceAuthorizationException extends RuntimeException {

    public InvalidServiceAuthorizationException(final String message, final String s2sToken, final Long hearingId) {
        super(String.format(message, s2sToken, hearingId));
    }

}
