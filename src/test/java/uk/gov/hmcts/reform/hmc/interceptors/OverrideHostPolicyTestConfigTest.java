package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OverrideHostPolicyTestConfigTest {

    @Test
    void permissiveOverrideHostPolicy_shouldAllowAnyNonEmptyUrl() {
        OverrideHostPolicy policy = new  OverrideHostPolicyTestConfig().overrideHostPolicy();

        assertThat(policy.isAllowed("http://example.com")).isTrue();
        assertThat(policy.isAllowed("  some-url  ")).isTrue();
    }

    @Test
    void overrideHostPolicy_shouldNotAllowNullOrEmptyUrl() {
        OverrideHostPolicyTestConfig config = new OverrideHostPolicyTestConfig();
        OverrideHostPolicy policy = config.overrideHostPolicy();

        assertThat(policy.isAllowed(null)).isFalse();
        assertThat(policy.isAllowed("")).isFalse();
        assertThat(policy.isAllowed("   ")).isFalse();
    }
}
