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
                .connectionString(applicationParams.getInternalOutboundConnectionString())
                .sender()
                .queueName(applicationParams.getInternalOutboundQueueName())
                .buildClient();
            log.debug("Connected to queue {}", applicationParams.getInternalOutboundQueueName());
            senderClient.sendMessage(new ServiceBusMessage(message));
            log.debug("Message has been sent to the queue {}", applicationParams.getInternalOutboundQueueName());
        } catch (Exception e) {
            log.error("Error while sending the message to queue:{}", e.getMessage());
        }
    }
}
