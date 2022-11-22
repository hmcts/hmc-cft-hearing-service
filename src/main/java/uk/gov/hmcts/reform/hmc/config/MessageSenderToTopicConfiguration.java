package uk.gov.hmcts.reform.hmc.config;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR_SENDING_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TOPIC_HMC_TO_CFT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.WRITE;

@Slf4j
@Component
public class MessageSenderToTopicConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToTopicConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessage(String message, String hmctsServiceId, String hearingId) {
        try {
            final ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getExternalConnectionString())
                .sender()
                .topicName(applicationParams.getExternalTopicName())
                .buildClient();

            log.debug("Connected to Topic {}", applicationParams.getExternalTopicName());
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(HMCTS_SERVICE_ID, hmctsServiceId);
            serviceBusMessage.getApplicationProperties().put(HEARING_ID, hearingId);
            log.debug("Sending request for hmctsServiceCode  :{} , {} ",hmctsServiceId, message);
            senderClient.sendMessage(serviceBusMessage);
            log.debug("Message has been sent to the topic {}", applicationParams.getExternalTopicName());
        } catch (Exception e) {
            log.error("Error while sending the message to topic:{}", e.getMessage());
            log.error(
                ERROR_SENDING_MESSAGE,
                CFT_HEARING_SERVICE,
                TOPIC_HMC_TO_CFT,
                WRITE,
                hearingId
            );
        }
    }
}
