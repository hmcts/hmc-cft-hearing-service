package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.hmc.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OverrideHostPolicyConfigIT extends BaseTest {

    @Autowired
    private OverrideHostPolicyConfig policy;

    @Test
    void should_use_pattern_from_application_yaml() {
        assertThat(policy.isAllowed("https://ccd-data-store-api-pr-3079.preview.platform.hmcts.net"));
        assertThat(policy.isAllowed("https://service-pr-123.aat.platform.hmcts.net"));
        assertFalse(policy.isAllowed("https://datastore.com"));
        assertFalse(policy.isAllowed(""));
        assertFalse(policy.isAllowed(" "));
        assertFalse(policy.isAllowed("http://service-pr-123.aat.platform.hmcts.net"));
        assertFalse(policy.isAllowed("http://pr-123.aat.platform.hmcts.net"));
        assertFalse(policy.isAllowed("http://invalidUrl"));
    }
}
