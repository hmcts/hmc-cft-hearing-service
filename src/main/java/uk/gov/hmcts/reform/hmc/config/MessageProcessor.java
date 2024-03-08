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

import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR_PROCESSING_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC_FROM_HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NO_DEFINED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.READ;

@Slf4j
@Service
public class MessageProcessor {

    private final ObjectMapper objectMapper;
    private final InboundQueueService inboundQueueService;
    private static final String MESSAGE_TYPE = "message_type";
    public static final String MISSING_MESSAGE_TYPE = "Message is missing custom header message_type";
    public static final String MESSAGE_PARSE_ERROR = "Unable to parse incoming message with id {}, {}";
    public static final String MESSAGE_ERROR = "Error for message with id ";
    public static final String WITH_ERROR = " with error ";
    public static final String MESSAGE_SUCCESS = "Message with id '{}' handled successfully";

    public MessageProcessor(ObjectMapper objectMapper,
                            InboundQueueService inboundQueueService) {
        this.objectMapper = objectMapper;
        this.inboundQueueService = inboundQueueService;
    }

    public void processMessage(ServiceBusReceivedMessageContext messageContext) {
        log.debug("In Queue config process message method ");
        var processingResult = tryProcessMessage(messageContext);
        if (processingResult.resultType.equals(MessageProcessingResultType.SUCCESS)) {
            log.info(MESSAGE_SUCCESS, messageContext.getMessage().getMessageId());
        }
    }

    public void processMessage(JsonNode message,
                               ServiceBusReceivedMessageContext messageContext)
            throws JsonProcessingException {
        log.debug("processing message in Message Processor " + message);
        Map<String, Object> applicationProperties = messageContext.getMessage().getApplicationProperties();
        if (applicationProperties.containsKey(MESSAGE_TYPE)) {
            try {
                log.debug("calling inbound process message");
                inboundQueueService.processMessage(message, messageContext);
            } catch (HearingNotFoundException ex) {
                log.error(MESSAGE_ERROR +  messageContext.getMessage().getMessageId() + WITH_ERROR + ex.getMessage());
            } catch (Exception ex) {
                log.error(MESSAGE_ERROR +  messageContext.getMessage().getMessageId() + WITH_ERROR + ex.getMessage());
                log.error(
                    ERROR_PROCESSING_MESSAGE,
                    CFT_HEARING_SERVICE,
                    HMC_FROM_HMI,
                    READ,
                    applicationProperties.getOrDefault(HEARING_ID,NO_DEFINED)
                );
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
        log.debug("tryProcessMessage method");
        try {
            processMessage(
                    convertMessage(messageContext.getMessage().getBody()),
                    messageContext
            );
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
