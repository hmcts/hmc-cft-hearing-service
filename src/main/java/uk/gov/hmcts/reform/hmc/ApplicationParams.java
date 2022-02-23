package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.inject.Named;
import javax.inject.Singleton;

@Getter
@Component
@Named
@Singleton
public class ApplicationParams {

    @Value("${jms.servicebus.internal.queues.inbound.connection-string}")
    private String internalInboundConnectionString;

    @Value("${jms.servicebus.internal.queues.inbound.queue-name}")
    private String internalInboundQueueName;

    @Value("${jms.servicebus.internal.queues.inbound.exponential-multiplier}")
    private String inboundExponentialMultiplier;

    @Value("${jms.servicebus.internal.queues.inbound.max-retry-attempts}")
    private int inboundMaxRetryAttempts;

    @Value("${jms.servicebus.external.connection-string}")
    private String externalConnectionString;

    @Value("${jms.servicebus.external.topic-name}")
    private String externalTopicName;

    @Value("${role.assignment.api.host}")
    private String roleAssignmentServiceHost;

    @Value("${jms.servicebus.internal.queues.outbound.connection-string}")
    private String internalOutboundConnectionString;

    @Value("${jms.servicebus.internal.queues.outbound.queue-name}")
    private String internalOutboundQueueName;

    public static String encode(final String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    public String roleAssignmentBaseUrl() {
        return roleAssignmentServiceHost + "/am/role-assignments";
    }

    public String amGetRoleAssignmentsUrl() {
        return roleAssignmentBaseUrl() + "/actors/{uid}";
    }
}
