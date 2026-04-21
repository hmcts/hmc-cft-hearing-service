package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private static final String HEADER_SIGNATURE = "X-Message-Signature";
    private static final String HEADER_SENDER = "X-Sender-Service";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String EXPECTED_INBOUND_SENDER = "HMI-Inbound-Adapter";
    private static final Duration MESSAGE_AGE_WARNING_THRESHOLD = Duration.ofMinutes(30);

    @Value("${hmac.secrets.hmi-to-hmc}")
    private String hmiToHmcSigningSecret;

    public MessageProcessor(ObjectMapper objectMapper,
                            InboundQueueService inboundQueueService) {
        this.objectMapper = objectMapper;
        this.inboundQueueService = inboundQueueService;
    }

    public void processMessage(ServiceBusReceivedMessageContext messageContext) {
        var processingResult = tryProcessMessage(messageContext);
        if (processingResult.resultType.equals(MessageProcessingResultType.SUCCESS)) {
            messageContext.complete();
            log.info(MESSAGE_SUCCESS, messageContext.getMessage().getMessageId());
        } else if (processingResult.resultType.equals(MessageProcessingResultType.UNRECOVERABLE_FAILURE)) {
            messageContext.deadLetter();
        }
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
        try {
            validateMessageSecurity(messageContext);
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
        } catch (SecurityException ex) {
            log.error("Security failure for message {}: {}",
                messageContext.getMessage().getMessageId(), ex.getMessage(), ex);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, ex);
        } catch (IllegalArgumentException ex) {
            // covers invalid Base64 signature and malformed security header values.
            log.error("Malformed security header for message {}: {}",
                messageContext.getMessage().getMessageId(), ex.getMessage(), ex);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, ex);
        } catch (IllegalStateException ex) {
            // covers HMAC calculation/configuration failures.
            log.error("Unable to validate message signature for message {}: {}",
                messageContext.getMessage().getMessageId(), ex.getMessage(), ex);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, ex);
        } catch (Exception ex) {
            log.warn("Unexpected Error");
            return new MessageProcessingResult(MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private void validateMessageSecurity(ServiceBusReceivedMessageContext context) {
        var message = context.getMessage();
        Map<String, Object> applicationProperties = message.getApplicationProperties();

        String signature = asString(applicationProperties.get(HEADER_SIGNATURE));
        String sender = asString(applicationProperties.get(HEADER_SENDER));
        String timestamp = asString(applicationProperties.get(HEADER_TIMESTAMP));

        if (signature == null || sender == null || timestamp == null) {
            throw new SecurityException("Missing required security headers");
        }

        if (!EXPECTED_INBOUND_SENDER.equals(sender)) {
            throw new SecurityException("Unexpected sender: " + sender);
        }

        String hearingId = asString(applicationProperties.get(HEARING_ID));
        String messageType = asString(applicationProperties.get(MESSAGE_TYPE));
        String bodyString = message.getBody().toString();
        warnIfTimestampOlderThanThreshold(message.getMessageId(), timestamp);
        String payloadToSign = buildPayloadToSign(bodyString, timestamp, sender, hearingId, messageType);
        String expectedSignature = hmacSha256Base64(payloadToSign, hmiToHmcSigningSecret);

        boolean matches = MessageDigest.isEqual(
            Base64.getDecoder().decode(signature),
            Base64.getDecoder().decode(expectedSignature)
        );

        if (!matches) {
            throw new SecurityException("Invalid message signature");
        }
    }

    private String buildPayloadToSign(String body,
                                      String timestamp,
                                      String sender,
                                      String hearingId,
                                      String messageType) {
        return String.join("|",
            "v1",
            sender,
            timestamp,
            messageType == null ? "" : messageType,
            hearingId == null ? "" : hearingId,
            body == null ? "" : body
        );
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private void warnIfTimestampOlderThanThreshold(String messageId, String timestamp) {
        try {
            Instant messageTime = Instant.parse(timestamp);
            Instant now = Instant.now();
            if (messageTime.isBefore(now.minus(MESSAGE_AGE_WARNING_THRESHOLD))) {
                log.warn("Message {} timestamp is older than {}: {}", messageId,
                    MESSAGE_AGE_WARNING_THRESHOLD, timestamp);
            }
        } catch (Exception ex) {
            log.warn("Unable to parse message timestamp for warning check on message {}: {}", messageId, timestamp);
        }
    }

    public String hmacSha256Base64(String payload, String base64Secret) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(base64Secret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate HMAC-SHA256", e);
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
