package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface InboundQueueService {

    void processMessage(JsonNode message,
                        ServiceBusReceivedMessageContext messageContext)
        throws JsonProcessingException;

    void catchExceptionAndUpdateHearing(Map<String, Object> applicationProperties, Exception exception);

}
