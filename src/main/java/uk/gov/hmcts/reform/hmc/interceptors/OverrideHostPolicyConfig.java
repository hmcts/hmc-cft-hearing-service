package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.regex.Pattern;

@Configuration
public class OverrideHostPolicyConfig {

    @Bean
    @Profile("!test")
    public OverrideHostPolicy strictOverrideHostPolicy() {
        Pattern allowed = Pattern.compile(
            "^https://([a-z0-9-]+\\.)*(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$",
            Pattern.CASE_INSENSITIVE
        );

        return url -> url != null && allowed.matcher(url.trim()).matches();
    }

    @Bean
    @Profile("test")
    public OverrideHostPolicy permissiveOverrideHostPolicy() {
        return url -> url != null && !url.trim().isEmpty();
    }
}
