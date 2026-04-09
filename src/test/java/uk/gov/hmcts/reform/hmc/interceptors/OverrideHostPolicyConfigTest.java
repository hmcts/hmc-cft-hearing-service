package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class OverrideHostPolicyConfigTest {

    private static final String REGEX =
        "^https://[a-z0-9-]+-pr-\\d+\\.(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$";

    @ParameterizedTest
    @ValueSource(strings = {
        "https://ccd-data-store-api-pr-1234.preview.platform.hmcts.net",
        "https://am-role-assignment-pr-56.aat.platform.hmcts.net:443/path",
        "https://service-pr-999.demo.platform.hmcts.net"
    })
    void should_allow_url_matching_configured_regex(String url) {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(REGEX);
        assertThat(policy.isAllowed(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com",
        "http://ccd.preview.platform.hmcts.neth",
        "https://am-role.preview.platform.hmcts.net.role.com"
    })
    void should_reject_url_not_matching_configured_regex(String url) {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(REGEX);
        assertThat(policy.isAllowed(url)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_null_empty_url(String url) {
        OverrideHostPolicyConfig policy = new OverrideHostPolicyConfig(".*");
        assertThat(policy.isAllowed(url)).isFalse();
    }

    @Test
    void should_reject_invalid_url() {
        assertThatThrownBy(() -> new OverrideHostPolicyConfig("*invalid[regex"))
            .isInstanceOf(PatternSyntaxException.class);
    }
}
