package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.BaseTest;

class MessageSenderToTopicConfigurationIT extends BaseTest  {

    @Test
    @EnabledIf(
        value = "isSlowTestsEnabled",
        disabledReason = "takes ages to finish")
    void shouldSuccessfullyProcessRequest() {
        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration =
            new MessageSenderToTopicConfiguration(applicationParams);
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      null);
    }

    @Test
    @EnabledIf(
        value = "isSlowTestsEnabled",
        disabledReason = "takes ages to finish")
    void shouldSuccessfullyProcessRequestWhenDeploymentIdIsPresent() {
        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration =
            new MessageSenderToTopicConfiguration(applicationParams);
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      "TEST");
    }
}
