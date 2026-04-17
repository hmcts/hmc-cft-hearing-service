package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MessageSenderToTopicConfigurationTest {

    private static final String SIGNING_SECRET = Base64.getEncoder().encodeToString(new byte[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 27, 28, 29, 30, 31
    });

    @Test
    void shouldAttachHmacHeadersToTopicMessage() {
        ApplicationParams applicationParams = mock(ApplicationParams.class);
        ServiceBusSenderClient senderClient = mock(ServiceBusSenderClient.class);

        MessageSenderToTopicConfiguration configuration = new MessageSenderToTopicConfiguration(
            applicationParams, SIGNING_SECRET, senderClient
        );

        configuration.sendMessage("{\"message\":\"ok\"}", "BBA3", "12345", "DEPLOY-1");

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());

        ServiceBusMessage sent = messageCaptor.getValue();
        String timestamp = sent.getApplicationProperties().get(MessageSenderToTopicConfiguration.HEADER_TIMESTAMP)
            .toString();

        assertNotNull(timestamp);
        assertEquals("BBA3", sent.getApplicationProperties().get("hmctsServiceId"));
        assertEquals("12345", sent.getApplicationProperties().get("hearing_id"));
        assertEquals("DEPLOY-1", sent.getApplicationProperties().get("hmctsDeploymentId"));
        assertEquals(MessageSenderToTopicConfiguration.SENDER_SERVICE,
                     sent.getApplicationProperties().get(MessageSenderToTopicConfiguration.HEADER_SENDER));

        String expectedPayload = configuration.buildPayloadToSign(
            "{\"message\":\"ok\"}", timestamp, "BBA3", "12345", "DEPLOY-1"
        );
        String actualSignature = sent.getApplicationProperties()
            .get(MessageSenderToTopicConfiguration.HEADER_SIGNATURE).toString();
        assertEquals(configuration.hmacSha256Base64(expectedPayload, SIGNING_SECRET), actualSignature);
    }

    @Test
    void shouldFailFastWhenSigningSecretIsMissingForTopicMessage() {
        ApplicationParams applicationParams = mock(ApplicationParams.class);
        ServiceBusSenderClient senderClient = mock(ServiceBusSenderClient.class);

        MessageSenderToTopicConfiguration configuration = new MessageSenderToTopicConfiguration(
            applicationParams, "", senderClient
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> configuration.sendMessage("{\"message\":\"ok\"}", "BBA3", "12345", "DEPLOY-1")
        );

        assertEquals("hmac.secrets.hmi-to-hmc must be configured", exception.getMessage());
        verify(senderClient, never()).sendMessage(any(ServiceBusMessage.class));
    }

    @Test
    void shouldNotSendTopicMessageWhenSigningSecretIsMalformed() {
        ApplicationParams applicationParams = mock(ApplicationParams.class);
        ServiceBusSenderClient senderClient = mock(ServiceBusSenderClient.class);

        MessageSenderToTopicConfiguration configuration = new MessageSenderToTopicConfiguration(
            applicationParams, "%%%invalid%%%", senderClient
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> configuration.sendMessage("{\"message\":\"ok\"}", "BBA3", "12345", "DEPLOY-1")
        );

        assertEquals("hmac.secrets.hmi-to-hmc must be valid Base64", exception.getMessage());
        verify(senderClient, never()).sendMessage(any(ServiceBusMessage.class));
    }

    @Test
    void shouldTreatBlankDeploymentIdAsAbsentInPropertyAndSignature() {
        ApplicationParams applicationParams = mock(ApplicationParams.class);
        ServiceBusSenderClient senderClient = mock(ServiceBusSenderClient.class);

        MessageSenderToTopicConfiguration configuration = new MessageSenderToTopicConfiguration(
            applicationParams, SIGNING_SECRET, senderClient
        );

        configuration.sendMessage("{\"message\":\"ok\"}", "BBA3", "12345", "   ");

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());

        ServiceBusMessage sent = messageCaptor.getValue();
        String timestamp = sent.getApplicationProperties().get(MessageSenderToTopicConfiguration.HEADER_TIMESTAMP)
            .toString();

        assertFalse(sent.getApplicationProperties().containsKey("hmctsDeploymentId"));
        String expectedPayload = configuration.buildPayloadToSign(
            "{\"message\":\"ok\"}", timestamp, "BBA3", "12345", null
        );
        String actualSignature = sent.getApplicationProperties()
            .get(MessageSenderToTopicConfiguration.HEADER_SIGNATURE).toString();
        assertEquals(configuration.hmacSha256Base64(expectedPayload, SIGNING_SECRET), actualSignature);
    }
}
