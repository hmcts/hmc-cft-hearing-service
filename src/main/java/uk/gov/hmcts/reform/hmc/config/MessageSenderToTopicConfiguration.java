package uk.gov.hmcts.reform.hmc.config;


import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AMQP_CACHE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMQP_CACHE_VALUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR_SENDING_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.TOPIC_HMC_TO_CFT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.WRITE;

@Slf4j
@Component
public class MessageSenderToTopicConfiguration {

    private final ApplicationParams applicationParams;
    private final ServiceBusSenderClient senderClient;
    private final String hmiToHmcSigningSecret;

    static final String HEADER_SIGNATURE = "X-Message-Signature";
    static final String HEADER_SENDER = "X-Sender-Service";
    static final String HEADER_TIMESTAMP = "X-Timestamp";
    static final String SENDER_SERVICE = "HMC-CFT-Hearing-Service";

    @Autowired
    public MessageSenderToTopicConfiguration(ApplicationParams applicationParams, @Value("${hmac.secrets.hmi-to-hmc}")
                                                 String hmiToHmcSigningSecret) {
        this(applicationParams, hmiToHmcSigningSecret, null);
    }

    MessageSenderToTopicConfiguration(ApplicationParams applicationParams,
                                      String hmiToHmcSigningSecret,
                                      ServiceBusSenderClient senderClient) {
        this.applicationParams = applicationParams;
        this.hmiToHmcSigningSecret = hmiToHmcSigningSecret;
        this.senderClient = senderClient;
    }

    public void sendMessage(String message, String hmctsServiceId, String hearingId, String deploymentId) {
        validateInput();

        try {
            log.debug("setting up the connection details for hearingId {}", hearingId);

            String timestamp = Instant.now().toString();
            String normalizedDeploymentId = StringUtils.hasText(deploymentId) ? deploymentId : null;
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.getApplicationProperties().put(HMCTS_SERVICE_ID, hmctsServiceId);
            serviceBusMessage.getApplicationProperties().put(HEARING_ID, hearingId);
            serviceBusMessage.getApplicationProperties().put(HEADER_SENDER, SENDER_SERVICE);
            serviceBusMessage.getApplicationProperties().put(HEADER_TIMESTAMP, timestamp);
            if (normalizedDeploymentId != null) {
                serviceBusMessage.getApplicationProperties().put(HMCTS_DEPLOYMENT_ID, normalizedDeploymentId);
            }

            String payloadToSign = buildPayloadToSign(message, timestamp, hmctsServiceId, hearingId,
                normalizedDeploymentId);
            String signature = hmacSha256Base64(payloadToSign, hmiToHmcSigningSecret);
            serviceBusMessage.getApplicationProperties().put(HEADER_SIGNATURE, signature);

            ServiceBusSenderClient sender = senderClient;
            if (sender == null) {
                sender = new ServiceBusClientBuilder()
                    .connectionString(applicationParams.getExternalConnectionString())
                    .configuration(new ConfigurationBuilder()
                                       .putProperty(AMQP_CACHE, AMQP_CACHE_VALUE)
                                       .build())
                    .sender()
                    .topicName(applicationParams.getExternalTopicName())
                    .buildClient();
            }

            log.debug("Connected to Topic {}", applicationParams.getExternalTopicName());
            log.debug("Sending message {} for hearingId {} with hmctsServiceId {} to topic {}",
                      message, hearingId, hmctsServiceId, applicationParams.getExternalTopicName());
            sender.sendMessage(serviceBusMessage);
            log.debug("Message has been sent to the topic {}", applicationParams.getExternalTopicName());
        } catch (Exception e) {
            log.error("Error while sending the message to topic:{}", e.getMessage());
            log.error(
                ERROR_SENDING_MESSAGE,
                CFT_HEARING_SERVICE,
                TOPIC_HMC_TO_CFT,
                WRITE,
                hearingId
            );
        }
    }

    private void validateInput() {
        if (!StringUtils.hasText(hmiToHmcSigningSecret)) {
            throw new IllegalStateException("hmac.secrets.hmi-to-hmc must be configured");
        }
        try {
            Base64.getDecoder().decode(hmiToHmcSigningSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("hmac.secrets.hmi-to-hmc must be valid Base64", e);
        }
    }

    String buildPayloadToSign(String body, String timestamp, String hmctsServiceId, String hearingId,
                              String deploymentId) {
        return String.join("|",
            "v1",
            SENDER_SERVICE,
            timestamp,
            "",
            hmctsServiceId == null ? "" : hmctsServiceId,
            hearingId == null ? "" : hearingId,
            deploymentId == null ? "" : deploymentId,
            body == null ? "" : body
        );
    }

    String hmacSha256Base64(String payload, String base64Secret) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(base64Secret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate HMAC-SHA256", e);
        }
    }
}
