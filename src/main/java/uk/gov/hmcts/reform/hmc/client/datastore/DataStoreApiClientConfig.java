package uk.gov.hmcts.reform.hmc.client.datastore;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;

public class DataStoreApiClientConfig {

    @Bean
    public SystemUserAuthHeadersInterceptor systemUserAuthHeadersInterceptor(SecurityUtils securityUtils) {
        return new SystemUserAuthHeadersInterceptor(securityUtils);
    }
}
