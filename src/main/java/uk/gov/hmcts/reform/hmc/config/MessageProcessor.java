package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.Map;

@Slf4j
@Service
public class MessageProcessor {

    private final ObjectMapper objectMapper;
    private final InboundQueueService inboundQueueService;
    private static final String MESSAGE_TYPE = "message_type";
    public static final String MISSING_MESSAGE_TYPE = "Message is missing custom header message_type";
    public static final String MESSAGE_PARSE_ERROR = "Unable to parse incoming message with id ";
    public static final String MESSAGE_ERROR = "Error for message with id ";
    public static final String WITH_ERROR = " with error ";

    public MessageProcessor(ObjectMapper objectMapper,
                            InboundQueueService inboundQueueService) {
        this.objectMapper = objectMapper;
        this.inboundQueueService = inboundQueueService;
    }

    public void processMessage(ServiceBusReceivedMessageContext messageContext) {
        log.debug("processMessage messageContext");
        var processingResult = tryProcessMessage(messageContext);
        // TODO: decide if to use processingResult or remove it.
    }

    public void processMessage(JsonNode message,
                               ServiceBusReceivedMessageContext messageContext)
            throws JsonProcessingException {

        Map<String, Object> applicationProperties = messageContext.getMessage().getApplicationProperties();

        if (applicationProperties.containsKey(MESSAGE_TYPE)) {
            try {
                inboundQueueService.processMessage(message, messageContext);
            } catch (HearingNotFoundException ex) {
                log.error(MESSAGE_ERROR +  messageContext.getMessage().getMessageId() + WITH_ERROR + ex.getMessage());
            } catch (Exception ex) {
                log.error(MESSAGE_ERROR + messageContext.getMessage().getMessageId() + WITH_ERROR + ex.getMessage());
                inboundQueueService.catchExceptionAndUpdateHearing(applicationProperties, ex);
            }
        } else {
            log.error(MISSING_MESSAGE_TYPE + " for message with message with id "
                          + messageContext.getMessage().getMessageId());
        }
    }

    public void processException(ServiceBusErrorContext context) {
        log.error("Processed message queue handle error {}", context.getErrorSource(), context.getException());
    }

    private MessageProcessingResult tryProcessMessage(ServiceBusReceivedMessageContext messageContext) {
        try {
            log.debug(
                    "Started processing message with ID {} (delivery {})",
                    messageContext.getMessage().getMessageId(),
                    messageContext.getMessage().getDeliveryCount() + 1
            );

            processMessage(
                    convertMessage(messageContext.getMessage().getBody()),
                    messageContext
            );

            log.debug("Processed message with ID {} processed successfully",
                    messageContext.getMessage().getMessageId());
            return new MessageProcessingResult(MessageProcessingResultType.SUCCESS);

            // TODO: decide what's Unrecoverable and what's Potentially Recoverable!
        } catch (JsonProcessingException ex) {
            log.error(MESSAGE_PARSE_ERROR,  messageContext.getMessage().getMessageId(), ex);
            inboundQueueService.catchExceptionAndUpdateHearing(messageContext.getMessage().getApplicationProperties(),
                    ex);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, ex);
        } catch (MalformedMessageException ex) {
            log.error("Invalid processed message with ID {}",  messageContext.getMessage().getMessageId(), ex);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            log.warn("Unexpected Error");
            return new MessageProcessingResult(MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private JsonNode convertMessage(BinaryData message) throws JsonProcessingException {
        return objectMapper.readTree(message.toString());
    }

    static class MessageProcessingResult {
        public final MessageProcessingResultType resultType;
        public final Exception exception;

        public MessageProcessingResult(MessageProcessingResultType resultType) {
            this(resultType, null);
        }

        public MessageProcessingResult(MessageProcessingResultType resultType, Exception exception) {
            this.resultType = resultType;
            this.exception = exception;
        }
    }

    enum MessageProcessingResultType {
        SUCCESS,
        UNRECOVERABLE_FAILURE,
        POTENTIALLY_RECOVERABLE_FAILURE
    }

}
