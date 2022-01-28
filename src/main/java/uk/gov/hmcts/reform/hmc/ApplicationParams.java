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

    @Value("${jms.servicebus.connection-string}")
    private String connectionString;

    @Value("${jms.servicebus.topic-name}")
    private String topicName;

    @Value("${jms.servicebus.subscription-name}")
    private String subscriptionName;

    @Value("${role.assignment.api.host}")
    private String roleAssignmentServiceHost;

    @Value("${jms.servicebus.hmi-queue-name}")
    private String queueName;

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
