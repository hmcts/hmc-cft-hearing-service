package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;

@Getter
public class FutureHearingServerException extends RuntimeException {

    public FutureHearingServerException(String message) {
        super(message);
    }
}
