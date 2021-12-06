package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyGetResponseFromHmi;

class MessageSenderToTopicConfigurationIT extends BaseTest {

    @MockBean
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Autowired
    private ApplicationParams applicationParams;

    @Test
    void shouldSuccessfullyProcessRequest() {
        stubSuccessfullyGetResponseFromHmi("{\n" +
                                               "  \"test\": \"value\"\n" +
                                               "}");
        messageSenderToTopicConfiguration.sendMessage("Test Message");
        verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any());
    }
}
