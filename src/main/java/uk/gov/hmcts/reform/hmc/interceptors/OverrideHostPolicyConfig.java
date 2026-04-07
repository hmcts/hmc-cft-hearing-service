package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class OverrideHostPolicyConfig implements OverrideHostPolicy {

    private final List<Pattern> allowedPatterns;
    private final boolean permissive;

    public OverrideHostPolicyConfig(
        Environment environment,
        @Value("${headerBased.allowed-override-hostpatterns:}") String overrideHostAllowList
    ) {
        this.permissive = environment.acceptsProfiles("test", "itest");

        if (overrideHostAllowList == null || overrideHostAllowList.isBlank()) {
            this.allowedPatterns = Collections.emptyList();
        } else {
            this.allowedPatterns = Arrays.stream(overrideHostAllowList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Pattern::compile)
                .toList();
        }
    }

    @Override
    public boolean isAllowed(String url) {
        if (permissive) {
            return url != null && !url.trim().isEmpty();
        }

        if (url == null) {
            return false;
        }

        Pattern defaultPattern = Pattern.compile(
            "^https://(?:[a-z0-9-]+\\.){0,5}(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$"
        );

        return allowedPatterns.stream()
            .anyMatch(pattern -> pattern.matcher(url.trim()).matches())
            || defaultPattern.matcher(url.trim()).matches();
    }
}
