package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyGetResponseFromHmi;

class MessageSenderConfigurationIT extends BaseTest {

    private final String caseListingId = "test-listing-id";

    @MockBean
    private MessageSenderConfiguration messageSenderConfiguration;

    @Test
    void shouldSuccessfullyProcessRequest() {
        stubSuccessfullyGetResponseFromHmi(caseListingId);
        messageSenderConfiguration.sendMessage("Test Message");
        verify(messageSenderConfiguration, times(1)).sendMessage(any());
    }
}
