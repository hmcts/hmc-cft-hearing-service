package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class OverrideHostPolicyTestConfig {

    @Bean
    public OverrideHostPolicy overrideHostPolicy() {
        return url -> url != null && !url.trim().isEmpty();
    }
}
