package uk.gov.hmcts.reform.hmc.interceptors;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

@Component
public class OverrideUrlValidator {

    private static final Pattern ALLOWED_HOST_PATTERN = Pattern.compile(
        "^(?:[a-z0-9-]+\\.)*(?:preview|aat|demo)\\.platform\\.hmcts\\.net$",
        Pattern.CASE_INSENSITIVE
    );

    public OverrideUrlValidator() {
    }

    public boolean isAllowed(String rawUrl) {
        if (rawUrl == null) {
            return false;
        }

        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        try {
            URI uri = URI.create(trimmed);

            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }

            String host = uri.getHost();
            return host != null && ALLOWED_HOST_PATTERN.matcher(host).matches();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
