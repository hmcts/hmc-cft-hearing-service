package uk.gov.hmcts.reform.hmc.exceptions;

public class HealthCheckActiveDirectoryException extends HealthCheckException {

    private static final String API_NAME = "ActiveDirectory";

    public HealthCheckActiveDirectoryException(String message) {
        super(message);
    }

    public HealthCheckActiveDirectoryException(String message, Integer statusCode, String errorMessage) {
        super(message, statusCode, errorMessage);
    }

    @Override
    public String getApiName() {
        return API_NAME;
    }
}
