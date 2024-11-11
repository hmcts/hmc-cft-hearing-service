package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;

@AutoConfigureAfter(QueueClientConfig.class)

@Slf4j
@Component
public class MessageReaderFromQueueConfiguration {

    private ServiceBusProcessorClient processedMessagesQueueClient;

    public MessageReaderFromQueueConfiguration(ServiceBusProcessorClient processedMessagesQueueClient) {
        this.processedMessagesQueueClient = processedMessagesQueueClient;
    }

    @PostConstruct()
    public void registerMessageHandlers() {
        log.info("Registering service bus processor client");
        processedMessagesQueueClient.start();
    }

}
