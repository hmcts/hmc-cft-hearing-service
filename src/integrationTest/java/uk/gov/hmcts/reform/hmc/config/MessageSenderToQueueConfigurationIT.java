package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;

class MessageSenderToQueueConfigurationIT extends BaseTest {

    @Autowired
    private ApplicationParams applicationParams;

    @Test
    void shouldSuccessfullyProcessRequest() {
        MessageSenderToQueueConfiguration messageSenderToQueueConfiguration =
            new MessageSenderToQueueConfiguration(applicationParams);
        messageSenderToQueueConfiguration.sendMessageToQueue("Test Message",1L,AMEND_HEARING,
                                                             null);
    }

    @Test
    void shouldSuccessfullyProcessRequestWhenDeploymentIdIsPresent() {
        MessageSenderToQueueConfiguration messageSenderToQueueConfiguration =
            new MessageSenderToQueueConfiguration(applicationParams);
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", true);
        messageSenderToQueueConfiguration.sendMessageToQueue("Test Message",1L,AMEND_HEARING,
                                                             "TEST");
    }
}
