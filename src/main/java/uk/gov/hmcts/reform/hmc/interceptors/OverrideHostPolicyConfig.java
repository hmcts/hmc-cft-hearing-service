package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.util.List;
import java.util.regex.Pattern;

@Configuration
public class OverrideHostPolicyConfig {

    private ApplicationParams applicationParams;

    public OverrideHostPolicyConfig(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    @Bean
    @Profile("!test")
    public OverrideHostPolicy strictOverrideHostPolicy() {

        List<String> overrideHostAllowList = applicationParams.getAllowedOverRideHostPatterns();

        List<Pattern> allowedPatterns = overrideHostAllowList == null
            ? new java.util.ArrayList<>()
            : overrideHostAllowList.stream()
            .map(Pattern::compile)
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        Pattern defaultPattern = Pattern.compile(
            "^https://(?:[a-z0-9-]+\\.){0,5}(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$"
        );

        allowedPatterns.add(defaultPattern);

        return url -> url != null
            && allowedPatterns.stream()
            .anyMatch(pattern -> pattern.matcher(url.trim()).matches());
    }

    @Bean
    @Profile("itest")
    public OverrideHostPolicy permissiveOverrideHostPolicy() {
        return url -> url != null && !url.trim().isEmpty();
    }
}
