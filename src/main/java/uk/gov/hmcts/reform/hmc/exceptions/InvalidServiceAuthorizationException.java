package uk.gov.hmcts.reform.hmc.exceptions;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidServiceAuthorizationException extends RuntimeException {

    public InvalidServiceAuthorizationException(final String message, final Long hearingId, final String s2sToken) {
        super(MessageFormatter.arrayFormat(message, new Object[] {hearingId, s2sToken}).getMessage());
    }

}
