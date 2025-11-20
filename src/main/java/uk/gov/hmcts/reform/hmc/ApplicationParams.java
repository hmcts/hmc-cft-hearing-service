package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.config.RoleAssignmentUrlManager;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;

@Getter
@Component
@Named
@Singleton
public class ApplicationParams {

    @Autowired
    private RoleAssignmentUrlManager roleAssignmentUrlManager;

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

    @Value("${jms.servicebus.internal.queues.outbound.connection-string}")
    private String internalOutboundConnectionString;

    @Value("${jms.servicebus.internal.queues.outbound.queue-name}")
    private String internalOutboundQueueName;

    @Value("${access-control.enabled}")
    private boolean accessControlEnabled;

    @Value("${fh.ad.client-id}")
    private String clientId;

    @Value("${fh.ad.client-secret}")
    private String clientSecret;

    @Value("${fh.ad.scope}")
    private String scope;

    @Value("${fh.ad.grant-type}")
    private String grantType;

    @Value("${fh.hmi.source-system}")
    private String sourceSystem;

    @Value("${fh.hmi.destination-system}")
    private String destinationSystem;

    @Value("${hmcts-deployment-id.enabled}")
    private boolean hmctsDeploymentIdEnabled;

    @Value("#{'${supportTools.service-whitelist}'.split(',')}")
    private List<String> authorisedSupportToolServices;

    @Value("#{'${supportTools.role-whitelist}'.split(',')}")
    private List<String> authorisedSupportToolRoles;


    public static String encode(final String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    public String roleAssignmentBaseUrl() {
        return roleAssignmentUrlManager.getActualHost() + "/am/role-assignments";
    }

    public String amGetRoleAssignmentsUrl() {
        return roleAssignmentBaseUrl() + "/actors/{uid}";
    }
}
