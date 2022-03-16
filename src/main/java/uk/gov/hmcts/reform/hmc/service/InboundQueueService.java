package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface InboundQueueService {

    void processMessage(JsonNode message) throws JsonProcessingException;
}
