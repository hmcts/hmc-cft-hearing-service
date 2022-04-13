package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;

@Getter
public class BadFutureHearingRequestException extends RuntimeException {

    public BadFutureHearingRequestException(String message) {
        super(message);
    }

}
