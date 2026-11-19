package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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

    private final ServiceBusSenderClient senderClient;

    public MessageSenderToQueueConfiguration(
        @Qualifier("queueSenderClient") ServiceBusSenderClient senderClient
    ) {
        this.senderClient = senderClient;
    }

    public void sendMessageToQueue(String message, Long hearingId, String messageType, String deploymentId) {
        try {
            log.debug("Sending request for hearing Id :{} with messageType : {}, {} ",hearingId, messageType, message);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(MESSAGE_TYPE, messageType);
            serviceBusMessage.getApplicationProperties().put(HEARING_ID, hearingId);
            if (!StringUtils.isEmpty(deploymentId)) {
                serviceBusMessage.getApplicationProperties().put(HMCTS_DEPLOYMENT_ID, deploymentId);
            }
            senderClient.sendMessage(serviceBusMessage);

            log.debug("Message has been sent to the queue {}", senderClient.getEntityPath());
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
