package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
@SuppressWarnings({"PMD.MissingSerialVersionUID"})
public class CaseCouldNotBeFoundException extends RuntimeException {

    public CaseCouldNotBeFoundException(String message) {
        super(message);
    }
}
