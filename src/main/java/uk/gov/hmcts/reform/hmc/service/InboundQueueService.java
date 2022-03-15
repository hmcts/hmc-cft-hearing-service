package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.config.MessageType;

public interface InboundQueueService {

    void processMessage(JsonNode message, MessageType messageType) throws JsonProcessingException;
}
