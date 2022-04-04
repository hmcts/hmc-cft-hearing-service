package uk.gov.hmcts.reform.hmc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class LinkedHearingNotValidForUnlinkingException extends RuntimeException {

    public LinkedHearingNotValidForUnlinkingException(List<String> errorMessages) {
        super(errorMessages.toString());
    }
}
