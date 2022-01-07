package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;


@Slf4j
@Component
public class MessageProcessor {

    private final ObjectMapper objectMapper;

    public MessageProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void processMessage(ServiceBusReceiverClient client, ServiceBusReceivedMessage message) {
        log.info("Received message with id '{}'", message.getMessageId());
        client.complete(message);
        log.info("Message with id '{}' handled successfully", message.getMessageId());
    }

}
