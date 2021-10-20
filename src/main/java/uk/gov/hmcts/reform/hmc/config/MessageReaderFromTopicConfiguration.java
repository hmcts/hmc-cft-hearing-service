package uk.gov.hmcts.reform.hmc.config;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MessageReaderFromTopicConfiguration {

    private final ApplicationParams applicationParams;
    private final MessageProcessor messageProcessor;

    public MessageReaderFromTopicConfiguration(ApplicationParams applicationParams,
                                               MessageProcessor messageProcessor) {
        this.applicationParams = applicationParams;
        this.messageProcessor = messageProcessor;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    @SuppressWarnings("squid:S2189")
    public void readMessageFromTopic() {
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString(applicationParams.getConnectionString())
            .processor()
            .topicName(applicationParams.getTopicName())
            .subscriptionName(applicationParams.getSubscriptionName())
            .processMessage(MessageReaderFromTopicConfiguration::processMessage)
            .processError(context ->processError(context, countdownLatch))
            .buildProcessorClient();

        log.info("Starting the processor");
        processorClient.start();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Stopping and closing the processor");
        processorClient.close();
    }

    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        log.info("Received message with id '{}'", message.getMessageId());
    }

    private static void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        log.error("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                          context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            log.error("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            log.error("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                              reason, exception.getMessage());

            countdownLatch.countDown();
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            log.error("Message lock lost for message: %s%n", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("Unable to sleep for period of time");
            }
        } else {
            log.error("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
                              reason, context.getException());
        }
    }
}
