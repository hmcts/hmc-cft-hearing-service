package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HealthCheckResponse;

public interface FutureHearingRepository {

    AuthenticationResponse retrieveAuthToken();

    HealthCheckResponse privateHealthCheck();

    void createLinkedHearingGroup(JsonNode data);

    void deleteLinkedHearingGroup(String requestId);

    void updateLinkedHearingGroup(String requestId, JsonNode body);
}
