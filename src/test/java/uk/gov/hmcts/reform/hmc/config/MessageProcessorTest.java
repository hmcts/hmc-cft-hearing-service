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
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.config.MessageProcessor.MISSING_MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.config.MessageProcessor.UNSUPPORTED_MESSAGE_TYPE;

class MessageProcessorTest {
    private static final String MESSAGE_TYPE = "message_type";

    private MessageProcessor messageProcessor;

    @Mock
    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private ServiceBusReceiverClient client;

    @Mock
    private ServiceBusReceivedMessage message;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        messageProcessor = new MessageProcessor(OBJECT_MAPPER);
    }

    @Test
    void shouldInitiateRequestHearing() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.REQUEST_HEARING);
        when(message.getApplicationProperties()).thenReturn(applicationProperties);
        when(message.getBody()).thenReturn(BinaryData.fromString("{ \"test\": \"name\"}"));
        BinaryData jsonMessage = BinaryData.fromString("{ \"test\": \"name\"}");
        JsonNode node = OBJECT_MAPPER.readTree(jsonMessage.toString());
        messageProcessor.processMessage(client, message);
        verify(OBJECT_MAPPER).treeToValue(node, HearingResponse.class);
    }

    @Test
    void shouldThrowErrorWhenMessageTypeNotCateredFor() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.AMEND_HEARING);
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        assertThatThrownBy(() -> messageProcessor.processMessage(anyData, applicationProperties))
            .isInstanceOf(MalformedMessageException.class)
            .hasMessageContaining(UNSUPPORTED_MESSAGE_TYPE);
    }

    @Test
    void shouldThrowErrorWhenMessageTypeIsNull() {
        Map<String, Object> applicationProperties = new HashMap<>();
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        assertThatThrownBy(() -> messageProcessor.processMessage(anyData, applicationProperties))
            .isInstanceOf(MalformedMessageException.class)
            .hasMessageContaining(MISSING_MESSAGE_TYPE);
    }

    @Test
    void shouldThrowErrorWhenNoMessageType() {
        Map<String, Object> applicationProperties = new HashMap<>();
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        assertThatThrownBy(() -> messageProcessor.processMessage(anyData, applicationProperties))
            .isInstanceOf(MalformedMessageException.class)
            .hasMessageContaining(MISSING_MESSAGE_TYPE);
    }

    @Test
    void shouldThrowErrorWhenNoSupportedMessageType() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, "invalid message type");
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        assertThatThrownBy(() -> messageProcessor.processMessage(anyData, applicationProperties))
            .isInstanceOf(MalformedMessageException.class)
            .hasMessageContaining(UNSUPPORTED_MESSAGE_TYPE);
    }
}
