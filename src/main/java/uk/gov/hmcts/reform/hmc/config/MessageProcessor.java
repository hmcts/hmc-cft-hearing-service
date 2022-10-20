package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.Map;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NO_DEFINED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.READ;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TYPE_OUTBOUND;
import static uk.gov.hmcts.reform.hmc.constants.Constants.WRITE;

@Slf4j
@Component
public class MessageProcessor {

    private final ObjectMapper objectMapper;
    private final InboundQueueService inboundQueueService;
    private static final String MESSAGE_TYPE = "message_type";
    public static final String MISSING_MESSAGE_TYPE = "Message is missing custom header message_type";
    public static final String MESSAGE_PARSE_ERROR = "Unable to parse incoming message with id ";
    public static final String MESSAGE_ERROR = "Error for message with id ";
    public static final String WITH_ERROR = " with error ";

    public MessageProcessor(ObjectMapper objectMapper, InboundQueueService inboundQueueService) {
        this.objectMapper = objectMapper;
        this.inboundQueueService = inboundQueueService;
    }

    public void processMessage(ServiceBusReceiverClient client, ServiceBusReceivedMessage message) {
        try {
            log.info("Received message with id '{}'", message.getMessageId());
            processMessage(
                convertMessage(message.getBody()),
                message.getApplicationProperties(),
                client, message
            );
            client.complete(message);
            log.info("Message with id '{}' handled successfully", message.getMessageId());

        } catch (Exception exception) {
            log.error(MESSAGE_PARSE_ERROR, message.getMessageId(), exception);
            log.error(
                "Error occurred during service bus processing. Service:{} . Type: {}. Method: {}. Hearing ID: {}.",
                CFT_HEARING_SERVICE,
                TYPE_OUTBOUND,
                READ,
                message.getApplicationProperties().getOrDefault(HEARING_ID,NO_DEFINED)
            );
            inboundQueueService.catchExceptionAndUpdateHearing(message.getApplicationProperties(), exception);
        }
    }

    public void processMessage(JsonNode message, Map<String, Object> applicationProperties,
                               ServiceBusReceiverClient client, ServiceBusReceivedMessage serviceBusReceivedMessage) {
        if (applicationProperties.containsKey(MESSAGE_TYPE)) {
            try {
                inboundQueueService.processMessage(message, applicationProperties, client, serviceBusReceivedMessage);
            } catch (HearingNotFoundException ex) {
                log.error(MESSAGE_ERROR + serviceBusReceivedMessage.getMessageId() + WITH_ERROR + ex.getMessage());
            } catch (Exception ex) {
                log.error(MESSAGE_ERROR + serviceBusReceivedMessage.getMessageId() + WITH_ERROR + ex.getMessage());
                log.error(
                    "Error occurred during service bus processing. Service:{} . Type: {}. Method: {}. Hearing ID: {}.",
                    CFT_HEARING_SERVICE,
                    TYPE_OUTBOUND,
                    WRITE,
                    applicationProperties.getOrDefault(HEARING_ID,NO_DEFINED)
                );
                inboundQueueService.catchExceptionAndUpdateHearing(applicationProperties, ex);
            }
        } else {
            log.error(MISSING_MESSAGE_TYPE + " for message with message with id "
                          + serviceBusReceivedMessage.getMessageId());
        }
    }

    private JsonNode convertMessage(BinaryData message) throws JsonProcessingException {
        return objectMapper.readTree(message.toString());
    }
}
