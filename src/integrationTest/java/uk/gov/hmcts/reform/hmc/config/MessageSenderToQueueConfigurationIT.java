package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MessageSenderToQueueConfigurationIT extends BaseTest {

    @MockBean
    private MessageSenderToQueueConfiguration messageSenderConfiguration;

    @Test
    void shouldSuccessfullyProcessRequest() {
        messageSenderConfiguration.sendMessageToQueue("Test Message");
        verify(messageSenderConfiguration, times(1)).sendMessageToQueue(any());
    }
}
