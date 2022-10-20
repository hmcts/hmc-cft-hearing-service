package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TYPE_INBOUND;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TYPE_OUTBOUND;
import static uk.gov.hmcts.reform.hmc.constants.Constants.WRITE;

@Slf4j
@Component
public class MessageSenderToQueueConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToQueueConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessageToQueue(String message, Long hearingId, String messageType) {
        try {
            final ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getInternalOutboundConnectionString())
                .sender()
                .queueName(applicationParams.getInternalOutboundQueueName())
                .buildClient();
            log.debug("Connected to queue {}", applicationParams.getInternalOutboundQueueName());
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(MESSAGE_TYPE, messageType);
            serviceBusMessage.getApplicationProperties().put(HEARING_ID, hearingId);
            senderClient.sendMessage(serviceBusMessage);

            log.debug("Message has been sent to the queue {}", applicationParams.getInternalOutboundQueueName());
        } catch (Exception e) {
            log.error(
                "Error occurred during service bus processing. Service:{} . Type: {}. Method: {}. Hearing ID: {}.",
                CFT_HEARING_SERVICE,
                TYPE_INBOUND,
                WRITE,
                hearingId
            );
            log.error("Error while sending the message to queue:{}", e.getMessage());
        }
    }
}
