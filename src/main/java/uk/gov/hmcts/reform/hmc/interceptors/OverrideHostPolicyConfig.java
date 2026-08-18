package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class OverrideHostPolicyConfig implements OverrideHostPolicy {

    private final Pattern allowedPattern;

    public OverrideHostPolicyConfig(
        @Value("${headerBased.allowed-override-hostpatterns}") String pattern) {
        this.allowedPattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isAllowed(String url) {
        return url != null && !url.trim().isEmpty() && allowedPattern.matcher(url).matches();
    }
}
