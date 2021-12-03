package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MessageSenderToTopicConfigurationIT extends BaseTest {

    @MockBean
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;


    @Disabled
    @Test
    void shouldSuccessfullyProcessRequest() {
        messageSenderToTopicConfiguration.sendMessage("Test Message");
        verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any());
    }
}
