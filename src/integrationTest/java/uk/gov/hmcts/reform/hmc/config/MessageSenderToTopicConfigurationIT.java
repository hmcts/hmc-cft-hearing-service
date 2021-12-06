package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @MockBean
    private ApplicationParams applicationParams;

    @Mock
    ServiceBusSenderClient serviceBusSenderClient;

    @Test
    void shouldSuccessfullyProcessRequest() {
        stubSuccessfullyGetResponseFromHmi("{\n"
                                               + "  \"test\": \"value\"\n"
                                               + "}");
        messageSenderToTopicConfiguration.sendMessage("Test Message");
        verify(messageSenderToTopicConfiguration, times(1)).sendMessage(any());
    }
}
