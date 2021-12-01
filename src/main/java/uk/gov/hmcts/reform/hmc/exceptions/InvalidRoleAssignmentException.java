package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class InvalidRoleAssignmentException extends ApiException {

    public InvalidRoleAssignmentException(final String message) {
        this(message, null);
    }

    public InvalidRoleAssignmentException(String message, Throwable e) {
        super(message, e);
    }
}
