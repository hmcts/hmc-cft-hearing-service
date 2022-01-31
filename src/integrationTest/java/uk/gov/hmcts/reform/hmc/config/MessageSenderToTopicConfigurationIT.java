package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

class MessageSenderToTopicConfigurationIT {

    @MockBean
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Autowired
    private ApplicationParams applicationParams;

    @Test
    void shouldSuccessfullyProcessRequest() {
        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration =
            new MessageSenderToTopicConfiguration(applicationParams);
        messageSenderToTopicConfiguration.sendMessage("Test Message");
    }
}
