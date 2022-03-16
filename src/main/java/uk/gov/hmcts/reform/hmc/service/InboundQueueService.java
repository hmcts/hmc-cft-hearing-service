package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface InboundQueueService {

    void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException;
}
