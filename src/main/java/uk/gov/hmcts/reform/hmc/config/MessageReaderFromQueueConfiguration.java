package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.time.Duration;

@Slf4j
@Component
public class MessageReaderFromQueueConfiguration {

    private final ApplicationParams applicationParams;
    private final MessageProcessor messageProcessor;

    public MessageReaderFromQueueConfiguration(ApplicationParams applicationParams, MessageProcessor messageProcessor) {
        this.applicationParams = applicationParams;
        this.messageProcessor = messageProcessor;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings("squid:S2189")
    public void readMessageFromTopic() {
        log.info("Creating service bus receiver client");

        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .connectionString(applicationParams.getInternalInboundConnectionString())
            .retryOptions(retryOptions())
            .receiver()
            .queueName(applicationParams.getInternalInboundQueueName())
            .buildClient();

        while (true) {
            receiveMessages(client);
        }
    }

    // handles received messages
    public void receiveMessages(ServiceBusReceiverClient client) {
        client.receiveMessages(1)
            .forEach(
                message -> {
                    log.info("message received with Id " + message.getMessageId()
                                 + " and message body " + message.getBody());
                    messageProcessor.processMessage(client, message);
                });
    }

    private AmqpRetryOptions retryOptions() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setMode(AmqpRetryMode.EXPONENTIAL)
            .setDelay(Duration.ofSeconds(Long.valueOf(applicationParams.getInboundExponentialMultiplier())));
        return retryOptions;
    }
}
