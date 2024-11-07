package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.BaseTest;

class MessageSenderToTopicConfigurationIT extends BaseTest  {

    @Test
    void shouldSuccessfullyProcessRequest() {
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      null);
    }

    @Test
    void shouldSuccessfullyProcessRequestWhenDeploymentIdIsPresent() {
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      "TEST");
    }
}
