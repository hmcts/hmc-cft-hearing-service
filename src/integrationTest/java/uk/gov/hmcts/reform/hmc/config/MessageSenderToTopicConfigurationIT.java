package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_SERVICE_ID;

class MessageSenderToTopicConfigurationIT extends BaseTest {

    @Autowired
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Test
    void sendsTheHearingMessage() {
        messageSenderToTopicConfiguration.sendMessage("Test Message", "ABA5", "123456", null);

        var message = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(topicSenderClient).sendMessage(message.capture());
        assertThat(message.getValue().getBody().toString()).isEqualTo("Test Message");
        assertThat(message.getValue().getApplicationProperties())
            .containsEntry(HMCTS_SERVICE_ID, "ABA5")
            .containsEntry(HEARING_ID, "123456")
            .doesNotContainKey(HMCTS_DEPLOYMENT_ID);
    }

    @Test
    void sendsTheDeploymentIdWhenPresent() {
        messageSenderToTopicConfiguration.sendMessage("Test Message", "ABA5", "123456", "TEST");

        var message = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(topicSenderClient).sendMessage(message.capture());
        assertThat(message.getValue().getApplicationProperties()).containsEntry(HMCTS_DEPLOYMENT_ID, "TEST");
    }
}
