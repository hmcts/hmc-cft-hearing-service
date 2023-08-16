package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;

import javax.inject.Inject;

class MessageSenderToTopicConfigurationIT extends BaseTest  {

    @Test
    void shouldSuccessfullyProcessRequest() {
        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration =
            new MessageSenderToTopicConfiguration(applicationParams);
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      null);
    }

    @Test
    void shouldSuccessfullyProcessRequestWhenDeploymentIdIsPresent() {
        MessageSenderToTopicConfiguration messageSenderToTopicConfiguration =
            new MessageSenderToTopicConfiguration(applicationParams);
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        messageSenderToTopicConfiguration.sendMessage("Test Message",
                                                      "Test service code","hearingID",
                                                      "ABA1");
    }
}
