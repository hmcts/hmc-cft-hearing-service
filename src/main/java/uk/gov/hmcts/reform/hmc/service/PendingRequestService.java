package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;

public interface PendingRequestService {

    PendingRequestEntity findById(Long id);

    void generatePendingRequest(JsonNode message, Long hearingId, String messageType, String deploymentId);

}
