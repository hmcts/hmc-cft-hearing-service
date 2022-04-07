package uk.gov.hmcts.reform.hmc.repository;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.futurehearing.ActiveDirectoryApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationRequest;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

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
    public HearingManagementInterfaceResponse deleteLinkedHearingGroup(String requestId) {
        String authorization = retrieveAuthToken().getAccessToken();
        try {
            return hmiClient.deleteLinkedHearingGroup(BEARER + authorization, requestId);
        } catch (Exception exception) {
            return getResponseFromListAssist(exception);
        }
    }

    private HearingManagementInterfaceResponse getResponseFromListAssist(Exception exception) {
        if (exception instanceof AuthenticationException || exception instanceof ResourceNotFoundException) {
            return new HearingManagementInterfaceResponse(400, exception.getMessage());
        } else {
            return new HearingManagementInterfaceResponse(500, exception.getMessage());
        }
    }
}
