package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageProcessorTest {
    private static final String MESSAGE_TYPE = "message_type";

    private MessageProcessor messageProcessor;

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private ServiceBusReceiverClient client;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private InboundQueueServiceImpl inboundQueueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
    }

    @Test
    void shouldInitiateHearingResponseRequest() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        applicationProperties.put("HEARING_ID", "20000000000");
        when(message.getApplicationProperties()).thenReturn(applicationProperties);
        when(message.getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\"}"));
        messageProcessor.processMessage(client, message);
        verify(inboundQueueService).processMessage(any(), any(), any(), any());
    }

    @Test
    void shouldInitiateErrorResponseRequest() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        when(message.getApplicationProperties()).thenReturn(applicationProperties);
        when(message.getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\"}"));
        messageProcessor.processMessage(client, message);
        verify(inboundQueueService).processMessage(any(), any(), any(), any());
    }

    @Test
    void shouldThrowErrorWhenMessageTypeIsNull() {
        Map<String, Object> applicationProperties = new HashMap<>();
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        messageProcessor.processMessage(anyData, applicationProperties, client, message);
        verify(inboundQueueService, times(0)).catchExceptionAndUpdateHearing(any(), any());
    }

    @Test
    void shouldLogErrorWhenNoMessageType() {
        Map<String, Object> applicationProperties = new HashMap<>();
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        messageProcessor.processMessage(anyData, applicationProperties, client, message);
        verify(inboundQueueService, times(0)).catchExceptionAndUpdateHearing(any(), any());
    }

    @Test
    void shouldCatchExceptionAndLog() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        when(message.getApplicationProperties()).thenReturn(applicationProperties);
        when(message.getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\"}"));
        ResourceNotFoundException exception = new ResourceNotFoundException("");
        doThrow(exception).when(inboundQueueService).processMessage(any(), any(), any(), any());
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        messageProcessor.processMessage(anyData, applicationProperties, client, message);
        verify(inboundQueueService, times(1)).processMessage(any(), any(), any(), any());
        verify(inboundQueueService, times(1)).catchExceptionAndUpdateHearing(any(), any());
    }

    @Test
    void shouldThrowErrorWhenParsingMessageBody() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        when(message.getApplicationProperties()).thenReturn(applicationProperties);
        when(message.getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\""));
        messageProcessor.processMessage(client, message);
        verify(inboundQueueService, times(0)).catchExceptionAndUpdateHearing(any(), any());
        verify(inboundQueueService, times(0)).processMessage(any(), any(), any(), any());
    }


}
