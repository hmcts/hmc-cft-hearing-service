package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

class OverrideHostPolicyConfigIT extends BaseTest {

    @Autowired
    private OverrideHostPolicyConfig policy;

    @ParameterizedTest
    @ValueSource(strings = {
        "https://ccd-data-store-api-pr-3079.preview.platform.hmcts.net",
        "https://service-pr-123.aat.platform.hmcts.net",
        "https://service-pr-999.demo.platform.hmcts.net"
    })
    void should_allow_url_matching_configured_regex(String url) {
        assertThat(policy.isAllowed(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://datastore.com",
        "http://service-pr-123.aat.platform.hmcts.net",
        "http://pr-123.aat.platform.hmcts.net",
        "http://invalidUrl"
    })
    void should_reject_url_not_matching_configured_regex(String url) {
        assertThat(policy.isAllowed(url)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_null_empty_url(String url) {
        assertThat(policy.isAllowed(url)).isFalse();
    }
}
