package uk.gov.hmcts.reform.hmc.exceptions;

public class ListAssistResponseException extends RuntimeException {

    public ListAssistResponseException(Long hearingId, String errorMessage) {
        super(String.format("Error received for hearing Id: %s with an error message of %s",
                            hearingId, errorMessage
        ));
    }
}
