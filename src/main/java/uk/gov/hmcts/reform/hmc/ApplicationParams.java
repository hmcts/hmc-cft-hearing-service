package uk.gov.hmcts.reform.hmc;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class ApplicationParams {
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
