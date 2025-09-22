package uk.gov.hmcts.reform.hmc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class is responsible for managing the URL of the role assignment API.
 * It is used to set the URL of the role assignment API and then retrieve it.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "role.assignment.api")
@RequestScope
public class RoleAssignmentUrlManager implements UrlManager {

    /**
     * Header name that should be used to get the URL of the role assignment API.
     */
    @Getter
    private String urlHeaderName;

    /**
     * Default URL of the role assignment API set via application properties.
     * This should not be modified at runtime. Changing at runtime would change
     * URL for all incoming requests that do not pass the URL via header.
     */
    @Getter
    private String host;

    /**
     * Actual host of the role assignment API that should be used for the current request.
     */
    private String actualHost;

    @Override
    public String getActualHost() {
        if (isBlank(actualHost)) {
            return host;
        } else {
            return actualHost;
        }
    }
}
