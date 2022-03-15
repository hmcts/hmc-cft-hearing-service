package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;
import uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class MessageProcessor {

    private final ObjectMapper objectMapper;
    private final InboundQueueService inboundQueueService;
    private static final String MESSAGE_TYPE = "message_type";
    public static final String UNSUPPORTED_MESSAGE_TYPE = "Message has unsupported value for message_type";
    public static final String MISSING_MESSAGE_TYPE = "Message is missing custom header message_type";
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

    public void processMessage(JsonNode message, Map<String, Object> applicationProperties) {
        if (applicationProperties.containsKey(MESSAGE_TYPE)) {
            MessageType messageType;
            try {
                messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
                switch (messageType) {
                    case REQUEST_HEARING:
                        inboundQueueService.processMessage(message, messageType);
                        break;
                    default:
                        throw new MalformedMessageException(UNSUPPORTED_MESSAGE_TYPE);
                }
            } catch (Exception exception) {
                throw new MalformedMessageException(UNSUPPORTED_MESSAGE_TYPE);
            }
        } else {
            throw new MalformedMessageException(MISSING_MESSAGE_TYPE);
        }
    }

    private JsonNode convertMessage(BinaryData message) throws JsonProcessingException {
        return objectMapper.readTree(message.toString());
    }
}
