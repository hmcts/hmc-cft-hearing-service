package uk.gov.hmcts.reform.hmc.exceptions;

public class MalformedMessageException extends RuntimeException {

    public MalformedMessageException(String message) {
        super(message);
    }
}
