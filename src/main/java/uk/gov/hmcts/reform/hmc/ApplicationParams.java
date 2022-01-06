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

    @Value("${jms.servicebus.internal.connection-string}")
    private String internalConnectionString;

    @Value("${jms.servicebus.internal.queue-name}")
    private String internalQueueName;

    @Value("${jms.servicebus.internal.exponential-multiplier}")
    private String exponentialMultiplier;

    @Value("${jms.servicebus.internal.max-retry-attempts}")
    private int maxRetryAttempts;

    @Value("${role.assignment.api.host}")
    private String roleAssignmentServiceHost;

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
