package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
