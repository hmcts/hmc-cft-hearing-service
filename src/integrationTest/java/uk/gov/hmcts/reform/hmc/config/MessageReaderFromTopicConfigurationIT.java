package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MessageReaderFromTopicConfigurationIT extends BaseTest {

    @MockBean
    private MessageReaderFromTopicConfiguration messageReaderFromTopicConfiguration;

    @Test
    void shouldSuccessfullyProcessRequest() {
        messageReaderFromTopicConfiguration.readMessageFromTopic();
        verify(messageReaderFromTopicConfiguration, times(2)).readMessageFromTopic();
    }
}
