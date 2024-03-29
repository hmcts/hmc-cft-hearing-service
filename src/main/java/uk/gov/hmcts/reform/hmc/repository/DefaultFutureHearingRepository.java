package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.futurehearing.ActiveDirectoryApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationRequest;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceApiClient;

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
}
