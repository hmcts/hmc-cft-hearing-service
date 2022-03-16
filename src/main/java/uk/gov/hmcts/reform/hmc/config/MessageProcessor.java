package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.Map;

@Slf4j
@Component
public class MessageProcessor {

    private final ObjectMapper objectMapper;
    private final InboundQueueService inboundQueueService;
    public static final String MESSAGE_PARSE_ERROR = "Unable to parse incoming message with id '{}'";

    public MessageProcessor(ObjectMapper objectMapper, InboundQueueService inboundQueueService) {
        this.objectMapper = objectMapper;
        this.inboundQueueService = inboundQueueService;
    }

    public void processMessage(ServiceBusReceiverClient client, ServiceBusReceivedMessage message) {
        try {
            log.info("Received message with id '{}'", message.getMessageId());
            processMessage(
                convertMessage(message.getBody()),
                message.getApplicationProperties()
            );
            client.complete(message);
            log.info("Message with id '{}' handled successfully", message.getMessageId());

        } catch (JsonProcessingException ex) {
            log.error(MESSAGE_PARSE_ERROR, message.getMessageId(), ex);
        }
    }

    public void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        inboundQueueService.processMessage(message, applicationProperties);
    }

    private JsonNode convertMessage(BinaryData message) throws JsonProcessingException {
        return objectMapper.readTree(message.toString());
    }
}
