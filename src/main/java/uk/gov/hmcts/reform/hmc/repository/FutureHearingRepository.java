package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;

public interface FutureHearingRepository {

    AuthenticationResponse retrieveAuthToken();

    void createLinkedHearingGroup(JsonNode data);

    void deleteLinkedHearingGroup(String requestId);

    void updateLinkedHearingGroup(String requestId, JsonNode body);
}
