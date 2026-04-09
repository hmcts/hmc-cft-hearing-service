package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OverrideHostPolicyConfigTest {

    private static final String REGEX =
        "^https://[a-z0-9-]+-pr-\\d+\\.(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$";

    @Test
    void should_allow_url_matching_configured_regex() {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(REGEX);
        assertThat(policy.isAllowed(
            "https://ccd-data-store-api-pr-1234.preview.platform.hmcts.net"));
        assertThat(policy.isAllowed(
            "https://am-role-assignment-pr-56.aat.platform.hmcts.net:443/path"));
        assertThat(policy.isAllowed(
            "https://service-pr-999.demo.platform.hmcts.net"));
    }

    @Test
    void should_reject_url_not_matching_configured_regex() {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(REGEX);

        assertFalse(policy.isAllowed("https://example.com"));
        assertFalse(policy.isAllowed("http://ccd.preview.platform.hmcts.net"));
        assertFalse(policy.isAllowed("https://am-role.preview.platform.hmcts.net.role.com"));
    }

    @Test
    void should_reject_invalid_url() {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(".*");
        assertFalse(policy.isAllowed(null));
        assertFalse(policy.isAllowed("   "));
        assertThrows(PatternSyntaxException.class,
                     () -> new OverrideHostPolicyConfig("*invalid[regex"));
    }
}
