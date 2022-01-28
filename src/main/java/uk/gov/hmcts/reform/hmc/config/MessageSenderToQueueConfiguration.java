package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

@Slf4j
@Component
public class MessageSenderToQueueConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToQueueConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessageToQueue(String message) {
        try {
            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getConnectionString())
                .sender()
                .queueName(applicationParams.getQueueName())
                .buildClient();
            log.debug("Connected to Queue {}", applicationParams.getQueueName());
            senderClient.sendMessage(new ServiceBusMessage(message));
            log.debug("Message has been sent to the Queue {}", applicationParams.getQueueName());
        } catch (Exception e) {
            log.error("Error while sending the message to queue:{}", e.getMessage());
        }
    }
}
