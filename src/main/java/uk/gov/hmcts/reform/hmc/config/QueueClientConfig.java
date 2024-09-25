package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

@Slf4j
@Configuration
public class QueueClientConfig {

    private final ApplicationParams applicationParams;

    public QueueClientConfig(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    @Bean("processed-messages-client")
    public ServiceBusProcessorClient processedMessageQueueClient(
            MessageProcessor messageHandler) {
        log.info("Creating & returning new service bus processor client.");
        return new ServiceBusClientBuilder()
            .connectionString(applicationParams.getInternalInboundConnectionString())
            .configuration(new com.azure.core.util.ConfigurationBuilder()
                               .putProperty("com.azure.messaging.eventhubs.v2", "true")
                               .build())
            .processor()
            .queueName(applicationParams.getInternalInboundQueueName())
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(messageHandler::processMessage)
            .processError(messageHandler::processException)
            .buildProcessorClient();
    }

}
