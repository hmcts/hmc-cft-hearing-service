package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;

import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OverrideHostPolicyConfigTest {

    @Test
    void should_allow_url_matching_configured_regex() {
        OverrideHostPolicyConfig policy =
            new OverrideHostPolicyConfig("^https://(?:[a-z0-9-]+\\.){0,5}(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$");

        assertTrue(policy.isAllowed("https://foo.preview.platform.hmcts.net"));
        assertTrue(policy.isAllowed("https://bar.aat.platform.hmcts.net:443/path"));
        assertTrue(policy.isAllowed("https://demo.platform.hmcts.net"));
    }

    @Test
    void should_reject_url_not_matching_configured_regex() {
        OverrideHostPolicyConfig policy =
            new OverrideHostPolicyConfig("^https://(?:[a-z0-9-]+\\.){0,5}(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$");

        assertFalse(policy.isAllowed("https://example.com"));
        assertFalse(policy.isAllowed("http://foo.preview.platform.hmcts.net"));
        assertFalse(policy.isAllowed("https://foo.preview.platform.hmcts.net.evil.com"));
    }

    @Test
    void should_reject_null_url() {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(".*");

        assertFalse(policy.isAllowed(null));
    }

    @Test
    void should_reject_non_matching_blank_url() {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(".*");

        assertFalse(policy.isAllowed("   "));
    }

    @Test
    void should_throw_when_regex_is_invalid() {
        assertThrows(PatternSyntaxException.class,
            () -> new OverrideHostPolicyConfig("*invalid[regex"));
    }
}
