package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface InboundQueueService {

    void processMessage(JsonNode message, Map<String, Object> applicationProperties,
                        ServiceBusReceiverClient client, ServiceBusReceivedMessage serviceBusReceivedMessage)
        throws JsonProcessingException;

    void catchExceptionAndUpdateHearing(Map<String, Object> applicationProperties, Exception exception);

}
