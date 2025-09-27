package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Request;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.futurehearing.ActiveDirectoryApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationRequest;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HealthCheckResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceApiClient;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckActiveDirectoryException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckHmiException;

import java.nio.charset.StandardCharsets;

@Slf4j
@Repository("defaultFutureHearingRepository")
public class DefaultFutureHearingRepository implements FutureHearingRepository {

    private final HearingManagementInterfaceApiClient hmiClient;
    private final ActiveDirectoryApiClient activeDirectoryApiClient;
    private final ApplicationParams applicationParams;
    private static final String BEARER = "Bearer ";

    public DefaultFutureHearingRepository(ActiveDirectoryApiClient activeDirectoryApiClient,
                                          ApplicationParams applicationParams,
                                          HearingManagementInterfaceApiClient hmiClient) {
        this.activeDirectoryApiClient = activeDirectoryApiClient;
        this.applicationParams = applicationParams;
        this.hmiClient = hmiClient;
    }

    public AuthenticationResponse retrieveAuthToken() {
        return activeDirectoryApiClient.authenticate(
            new AuthenticationRequest(
                applicationParams.getGrantType(),
                applicationParams.getClientId(), applicationParams.getScope(),
                applicationParams.getClientSecret()
            ).getRequest());
    }

    @Override
    public HealthCheckResponse privateHealthCheck() {
        String authorization;

        try {
            log.debug("Retrieving authorization token for HMI private health check");
            authorization = retrieveAuthToken().getAccessToken();
            log.debug("Authorization token retrieved successfully for HMI private health check");
        } catch (BadFutureHearingRequestException e) {
            logDebugHealthCheckActiveDirectoryException(e.getClass().getSimpleName());
            throw new HealthCheckActiveDirectoryException(e.getMessage(), e.getStatusCode(), e.getErrorMessage());
        } catch (FutureHearingServerException e) {
            logDebugHealthCheckActiveDirectoryException(e.getClass().getSimpleName());
            throw new HealthCheckActiveDirectoryException(e.getMessage());
        } catch (RetryableException e) {
            logDebugHealthCheckActiveDirectoryException(e);
            throw new HealthCheckActiveDirectoryException("Connection/Read timeout");
        }

        try {
            log.debug("Calling HMI private health check");
            return hmiClient.privateHealthCheck(BEARER + authorization);
        } catch (BadFutureHearingRequestException e) {
            logDebugHealthCheckHmiException(e);
            throw new HealthCheckHmiException(e.getMessage(), e.getStatusCode(), e.getErrorMessage());
        } catch (FutureHearingServerException e) {
            logDebugHealthCheckHmiException(e);
            throw new HealthCheckHmiException(e.getMessage());
        }
    }

    @Override
    public void createLinkedHearingGroup(JsonNode data) {
        log.debug("Request sent to FH : {}", data.toString());
        String authorization = retrieveAuthToken().getAccessToken();
        hmiClient.createLinkedHearingGroup(BEARER + authorization, data);
    }

    @Override
    public void deleteLinkedHearingGroup(String requestId) {
        String authorization = retrieveAuthToken().getAccessToken();
        hmiClient.deleteLinkedHearingGroup(BEARER + authorization, requestId);
    }

    @Override
    public void updateLinkedHearingGroup(String requestId, JsonNode data) {
        log.debug("Request sent to FH : {}", data.toString());
        String authorization = retrieveAuthToken().getAccessToken();
        hmiClient.updateLinkedHearingGroup(BEARER + authorization, requestId, data);
    }

    private void logDebugHealthCheckActiveDirectoryException(RetryableException retryableException) {
        Request request = retryableException.request();
        String requestBody = request.body() == null ? "N/A" : new String(request.body(), StandardCharsets.UTF_8);
        log.debug("Request to Active Directory timed out - "
                      + "URL: {}, Method: {}, Body: {}", request.url(), request.httpMethod(), requestBody);

        logDebugHealthCheckActiveDirectoryException(retryableException.getClass().getSimpleName());
    }

    private void logDebugHealthCheckActiveDirectoryException(String exceptionClassName) {
        log.debug("Failed to get authorization token for HMI health check. "
                      + "Converting {} exception to HealthCheckActiveDirectoryException.", exceptionClassName);
    }

    private void logDebugHealthCheckHmiException(Exception e) {
        log.debug("HMI health check failed. Converting {} exception to HealthCheckHmiException.",
                  e.getClass().getSimpleName());
    }
}
