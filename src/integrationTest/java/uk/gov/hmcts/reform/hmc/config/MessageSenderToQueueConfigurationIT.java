package uk.gov.hmcts.reform.hmc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;

class MessageSenderToQueueConfigurationIT  {

    @MockBean
    private MessageSenderToQueueConfiguration messageSenderConfiguration;

    @Autowired
    private ApplicationParams applicationParams;

    @Test
    void shouldSuccessfullyProcessRequest() {
        MessageSenderToQueueConfiguration messageSenderToQueueConfiguration =
            new MessageSenderToQueueConfiguration(applicationParams);
        messageSenderToQueueConfiguration.sendMessageToQueue("Test Message",1L,AMEND_HEARING);
    }
}
