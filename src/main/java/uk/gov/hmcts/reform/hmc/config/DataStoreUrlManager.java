package uk.gov.hmcts.reform.hmc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class is responsible for managing the URL of the Data Store API.
 * It is used to set the URL of the Data Store API and then retrieve it.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ccd.data-store")
@RequestScope
public class DataStoreUrlManager implements UrlManager {

    private String clientName;

    /**
     * Header name that should be used to get the URL of the Data Store API.
     */
    private String urlHeaderName;

    /**
     * Default URL of the Data Store API set via application properties.
     * This should not be modified at runtime. Changing at runtime would change
     * URL for all incoming requests that do not pass the URL via header.
     */
    private String host;

    /**
     * Actual host of the Data Store API that should be used for the current request.
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
