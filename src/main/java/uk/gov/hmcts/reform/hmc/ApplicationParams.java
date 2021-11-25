package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;

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

    @Value("${idam.caa.username}")
    private String caaSystemUserId;

    @Value("${idam.caa.password}")
    private String caaSystemUserPassword;

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

    public String getCaaSystemUserId() {
        return caaSystemUserId;
    }

    public String getCaaSystemUserPassword() {
        return caaSystemUserPassword;
    }
}
