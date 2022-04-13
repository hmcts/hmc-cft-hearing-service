package uk.gov.hmcts.reform.hmc.exceptions;

import lombok.Getter;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;

@Getter
public class FutureHearingServerException extends RuntimeException {

    private final ErrorDetails errorDetails;

    public FutureHearingServerException(String message, ErrorDetails errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
}
