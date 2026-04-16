package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MessageProcessorTest {
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEADER_SIGNATURE = "X-Message-Signature";
    private static final String HEADER_SENDER = "X-Sender-Service";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEARING_ID = "hearing_id";
    private static final String SENDER = "HMI-Inbound-Adapter";

    private static final String hmiToHmcSigningSecret = Base64.getEncoder().encodeToString(new byte[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 27, 28, 29, 30, 31
    });

    private MessageProcessor messageProcessor;

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private ServiceBusReceivedMessageContext messageContext;

    @Mock
    private InboundQueueServiceImpl inboundQueueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        ReflectionTestUtils.setField(messageProcessor, "hmiToHmcSigningSecret", hmiToHmcSigningSecret);
    }

    @Test
    void shouldInitiateHearingResponseRequest() throws JsonProcessingException {
        String body = "{ \"test\": \"name\"}";
        String timestamp = Instant.now().toString();

        Map<String, Object> applicationProperties =
            signedProps(body, MessageType.HEARING_RESPONSE.name(), "20000000000", timestamp);

        given(message.getApplicationProperties()).willReturn(applicationProperties);
        given(message.getBody()).willReturn(BinaryData.fromString(body));
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getMessageId()).willReturn("100001");
        given(messageContext.getMessage().getDeliveryCount()).willReturn(1L);
        messageProcessor.processMessage(messageContext);
        verify(inboundQueueService).processMessage(any(), any());
        verify(messageContext).complete();
    }

    @Test
    void shouldInitiateErrorResponseRequest() throws JsonProcessingException {
        String body = "{ \"test\": \"name\"}";
        String timestamp = Instant.now().toString();

        Map<String, Object> applicationProperties =
            signedProps(body, MessageType.ERROR.name(), "20000000000", timestamp);

        given(message.getBody()).willReturn(BinaryData.fromString(body));
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getMessageId()).willReturn("100001");
        given(messageContext.getMessage().getDeliveryCount()).willReturn(1L);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        messageProcessor.processMessage(messageContext);
        verify(inboundQueueService).processMessage(any(), any());
        verify(messageContext).complete();
    }

    @Test
    void shouldThrowErrorWhenMessageTypeIsNull() {
        Map<String, Object> applicationProperties = new HashMap<>();
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getMessageId()).willReturn("100001");
        given(messageContext.getMessage().getDeliveryCount()).willReturn(1L);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        try {
            messageProcessor.processMessage(anyData, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        verify(inboundQueueService, times(0)).catchExceptionAndUpdateHearing(any(), any());
    }

    @Test
    void shouldCatchExceptionAndLog() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE.name());
        given(messageContext.getMessage()).willReturn(message);
        when(messageContext.getMessage().getApplicationProperties()).thenReturn(applicationProperties);
        when(messageContext.getMessage().getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\"}"));
        ResourceNotFoundException exception = new ResourceNotFoundException("");
        doThrow(exception).when(inboundQueueService).processMessage(any(), any());
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        messageProcessor.processMessage(anyData, messageContext);
        verify(inboundQueueService, times(1)).processMessage(any(), any());
        verify(inboundQueueService, times(1)).catchExceptionAndUpdateHearing(any(), any());
    }

    @Test
    void shouldThrowErrorWhenParsingMessageBody() throws JsonProcessingException {
        // invalid JSON
        String body = "{ \"invalid\": \"name\", }";
        String timestamp = Instant.now().toString();

        Map<String, Object> applicationProperties =
            signedProps(body, MessageType.ERROR.name(), "20000000000", timestamp);

        given(messageContext.getMessage()).willReturn(message);
        when(messageContext.getMessage().getApplicationProperties()).thenReturn(applicationProperties);
        when(messageContext.getMessage().getBody()).thenReturn(BinaryData.fromString(body));
        messageProcessor.processMessage(messageContext);
        verify(inboundQueueService, times(1)).catchExceptionAndUpdateHearing(any(), any());
        verify(inboundQueueService, times(0)).processMessage(any(), any());
        verify(messageContext).deadLetter();
    }

    @Test
    void shouldProcessMessageWhenSignatureValid() throws Exception {
        String messageType = MessageType.HEARING_RESPONSE.name();
        String hearingId = "12345";
        String timestamp = Instant.now().toString();
        String body = "{\"test\":\"name\"}";

        Map<String, Object> props = new HashMap<>();
        props.put(MESSAGE_TYPE, messageType);
        props.put(HEARING_ID, hearingId);
        props.put(HEADER_SENDER, SENDER);
        props.put(HEADER_TIMESTAMP, timestamp);

        String payloadToSign = String.join("|",
            "v1",
            SENDER,
            timestamp,
            messageType,
            hearingId,
            body
        );

        String signatureB64 = messageProcessor.hmacSha256Base64(payloadToSign, hmiToHmcSigningSecret);
        props.put(HEADER_SIGNATURE, signatureB64);

        when(message.getApplicationProperties()).thenReturn(props);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
        when(message.getMessageId()).thenReturn("mid-1");
        when(messageContext.getMessage()).thenReturn(message);

        messageProcessor.processMessage(messageContext);

        verify(inboundQueueService, times(1)).processMessage(any(), eq(messageContext));
        verify(messageContext).complete();
    }

    @Test
    void shouldNotProcessMessageWhenSignatureMismatch() throws JsonProcessingException {
        String messageType = MessageType.HEARING_RESPONSE.name();
        String hearingId = "12345";
        String timestamp = Instant.now().toString();

        Map<String, Object> props = new HashMap<>();
        props.put(MESSAGE_TYPE, messageType);
        props.put(HEARING_ID, hearingId);
        props.put(HEADER_SENDER, SENDER);
        props.put(HEADER_TIMESTAMP, timestamp);
        props.put(HEADER_SIGNATURE, Base64.getEncoder().encodeToString("bad".getBytes(StandardCharsets.UTF_8)));

        ServiceBusReceivedMessage sbMessage = mock(ServiceBusReceivedMessage.class);
        String body = "{\"test\":\"name\"}";
        when(sbMessage.getApplicationProperties()).thenReturn(props);
        when(sbMessage.getBody()).thenReturn(BinaryData.fromString(body));
        when(sbMessage.getMessageId()).thenReturn("mid-2");

        ServiceBusReceivedMessageContext context = mock(ServiceBusReceivedMessageContext.class);
        when(context.getMessage()).thenReturn(sbMessage);

        messageProcessor.processMessage(context);

        verify(inboundQueueService, never()).processMessage(any(), any());
        verify(context).deadLetter();
    }

    @Test
    void shouldNotProcessMessageWhenSecurityHeadersMissing() throws JsonProcessingException {
        Map<String, Object> props = new HashMap<>();
        props.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE.name());
        props.put(HEARING_ID, "12345");

        String body = "{\"test\":\"name\"}";
        when(message.getApplicationProperties()).thenReturn(props);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
        when(message.getMessageId()).thenReturn("mid-missing");
        when(messageContext.getMessage()).thenReturn(message);

        messageProcessor.processMessage(messageContext);

        verify(inboundQueueService, never()).processMessage(any(), any());
        verify(inboundQueueService, never()).catchExceptionAndUpdateHearing(any(), any());
        verify(messageContext).deadLetter();
    }

    @Test
    void shouldNotProcessMessageWhenSenderUnexpected() throws JsonProcessingException {
        String body = "{\"test\":\"name\"}";
        String timestamp = Instant.now().toString();
        Map<String, Object> props = signedProps(body, MessageType.HEARING_RESPONSE.name(), "12345", timestamp);
        props.put(HEADER_SENDER, "Unknown-Service");

        when(message.getApplicationProperties()).thenReturn(props);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
        when(message.getMessageId()).thenReturn("mid-sender");
        when(messageContext.getMessage()).thenReturn(message);

        messageProcessor.processMessage(messageContext);

        verify(inboundQueueService, never()).processMessage(any(), any());
        verify(messageContext).deadLetter();
    }

    @Test
    void shouldProcessMessageWhenTimestampOldButSignatureValid() throws JsonProcessingException {
        String body = "{\"test\":\"name\"}";
        String oldTimestamp = "2026-01-01T00:00:00Z";
        Map<String, Object> props = signedProps(body, MessageType.HEARING_RESPONSE.name(), "12345", oldTimestamp);

        when(message.getApplicationProperties()).thenReturn(props);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
        when(message.getMessageId()).thenReturn("mid-old");
        when(messageContext.getMessage()).thenReturn(message);

        messageProcessor.processMessage(messageContext);

        verify(inboundQueueService).processMessage(any(), eq(messageContext));
        verify(messageContext).complete();
    }

    @Test
    void shouldNotProcessMessageWhenSignatureNotBase64() throws JsonProcessingException {
        String body = "{\"test\":\"name\"}";
        String timestamp = Instant.now().toString();
        Map<String, Object> props = signedProps(body, MessageType.HEARING_RESPONSE.name(), "12345", timestamp);
        props.put(HEADER_SIGNATURE, "%%%not-base64%%%");

        when(message.getApplicationProperties()).thenReturn(props);
        when(message.getBody()).thenReturn(BinaryData.fromString(body));
        when(message.getMessageId()).thenReturn("mid-badsig");
        when(messageContext.getMessage()).thenReturn(message);

        messageProcessor.processMessage(messageContext);

        verify(inboundQueueService, never()).processMessage(any(), any());
        verify(messageContext).deadLetter();
    }

    private Map<String, Object> signedProps(String body, String messageType, String hearingId, String timestamp) {
        Map<String, Object> props = new HashMap<>();
        props.put(MESSAGE_TYPE, messageType);
        props.put(HEARING_ID, hearingId);
        props.put(HEADER_SENDER, SENDER);
        props.put(HEADER_TIMESTAMP, timestamp);

        String payloadToSign = String.join("|",
            "v1",
            SENDER,
            timestamp,
            messageType,
            hearingId,
            body
        );

        props.put(HEADER_SIGNATURE, messageProcessor.hmacSha256Base64(payloadToSign, hmiToHmcSigningSecret));
        return props;
    }
}
