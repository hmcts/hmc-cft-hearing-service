package uk.gov.hmcts.reform.hmc.config;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

@Slf4j
@Component
public class MessageSenderToTopicConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToTopicConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessage(String message) {
        try {

            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getExternalConnectionString())
                .sender()
                .topicName(applicationParams.getExternalTopicName())
                .buildClient();

            log.debug("Connected to Topic {}", applicationParams.getExternalTopicName());
            senderClient.sendMessage(new ServiceBusMessage(message));
            log.debug("Message has been sent to the topic {}", applicationParams.getExternalTopicName());
        } catch (Exception e) {
            log.error("Error while sending the message to topic:{}", e.getMessage());
        }
    }
}
