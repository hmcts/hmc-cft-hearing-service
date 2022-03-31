package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceResponse;

public interface FutureHearingRepository {

    AuthenticationResponse retrieveAuthToken();

    HearingManagementInterfaceResponse createLinkedHearingGroup(JsonNode data);

}
