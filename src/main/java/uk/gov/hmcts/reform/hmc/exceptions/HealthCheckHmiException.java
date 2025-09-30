package uk.gov.hmcts.reform.hmc.exceptions;

public class HealthCheckHmiException extends HealthCheckException {

    private static final String API_NAME = "HearingManagementInterface";

    public HealthCheckHmiException(String message) {
        super(message);
    }

    public HealthCheckHmiException(String message, Integer statusCode, String errorResponse) {
        super(message, statusCode, errorResponse);
    }

    @Override
    public String getApiName() {
        return API_NAME;
    }
}
