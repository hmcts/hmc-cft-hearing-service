package uk.gov.hmcts.reform.hmc.config;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AMQP_CACHE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMQP_CACHE_VALUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR_PROCESSING_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC_TO_HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.WRITE;

@Slf4j
@Component
public class MessageSenderToQueueConfiguration {

    private final ApplicationParams applicationParams;

    public MessageSenderToQueueConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    public void sendMessageToQueue(String message, Long hearingId, String messageType, String deploymentId) {
        try {
            final ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(applicationParams.getInternalOutboundConnectionString())
                .configuration(new ConfigurationBuilder()
                                   .putProperty(AMQP_CACHE, AMQP_CACHE_VALUE)
                                   .build())
                .sender()
                .queueName(applicationParams.getInternalOutboundQueueName())
                .buildClient();
            log.debug("Connected to queue {}", applicationParams.getInternalOutboundQueueName());
            log.debug("Sending request for hearing Id :{} with messageType : {}, {} ",hearingId, messageType, message);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(MESSAGE_TYPE, messageType);
            serviceBusMessage.getApplicationProperties().put(HEARING_ID, hearingId);
            if (!StringUtils.isEmpty(deploymentId)) {
                serviceBusMessage.getApplicationProperties().put(HMCTS_DEPLOYMENT_ID, deploymentId);
            }
            senderClient.sendMessage(serviceBusMessage);

            log.debug("Message has been sent to the queue {}", applicationParams.getInternalOutboundQueueName());
        } catch (Exception e) {
            log.error(
                ERROR_PROCESSING_MESSAGE,
                CFT_HEARING_SERVICE,
                HMC_TO_HMI,
                WRITE,
                hearingId
            );
            log.error("Error while sending the message to queue:{}", e.getMessage());
        }
    }
}
