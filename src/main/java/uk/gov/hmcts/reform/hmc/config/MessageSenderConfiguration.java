package uk.gov.hmcts.reform.hmc.config;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

@Slf4j
@Component
public class MessageSenderConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessage(String message) {
        try {

            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getOutboundConnectionString())
                .sender()
                .topicName(applicationParams.getOutboundTopicName())
                .buildClient();

            log.debug("Connected to Topic {}", applicationParams.getInboundTopicName());
            senderClient.sendMessage(new ServiceBusMessage(message));
            log.debug("Message has been sent to the topic {}", applicationParams.getInboundTopicName());
        } catch (Exception e) {
            log.error("Error while sending the message to topic:{}", e.getMessage());
        }
    }
}
