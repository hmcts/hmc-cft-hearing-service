package uk.gov.hmcts.reform.hmc.config;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_SERVICE_ID;

@Slf4j
@Component
public class MessageSenderToTopicConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToTopicConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessage(String message, String hmctsServiceId) {
        try {
            log.debug("Connected to Topic {}", applicationParams.getExternalTopicName());
            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getExternalConnectionString())
                .sender()
                .topicName(applicationParams.getExternalTopicName())
                .buildClient();
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(HMCTS_SERVICE_ID, hmctsServiceId);
            log.debug("Sending request for hmctsServiceCode  :{} , {} ",hmctsServiceId, message);
            senderClient.sendMessage(serviceBusMessage);
            log.debug("Message has been sent to the topic {}", applicationParams.getExternalTopicName());
        } catch (Exception e) {
            log.error("Error while sending the message to topic:{}", e.getMessage());
        }
    }
}
