package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;

class MessageSenderToQueueConfigurationIT extends BaseTest {

    @Autowired
    private MessageSenderToQueueConfiguration messageSenderToQueueConfiguration;

    @Test
    void sendsTheHearingMessage() {
        messageSenderToQueueConfiguration.sendMessageToQueue("Test Message", 1L, AMEND_HEARING, null);

        var message = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient).sendMessage(message.capture());
        assertThat(message.getValue().getBody().toString()).isEqualTo("Test Message");
        assertThat(message.getValue().getApplicationProperties())
            .containsEntry(HEARING_ID, 1L)
            .containsEntry(MESSAGE_TYPE, AMEND_HEARING)
            .doesNotContainKey(HMCTS_DEPLOYMENT_ID);
    }

    @Test
    void sendsTheDeploymentIdWhenPresent() {
        messageSenderToQueueConfiguration.sendMessageToQueue("Test Message", 1L, AMEND_HEARING, "TEST");

        var message = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient).sendMessage(message.capture());
        assertThat(message.getValue().getApplicationProperties()).containsEntry(HMCTS_DEPLOYMENT_ID, "TEST");
    }
}
