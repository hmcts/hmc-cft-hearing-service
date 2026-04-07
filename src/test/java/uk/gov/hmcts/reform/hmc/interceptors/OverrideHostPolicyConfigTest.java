package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideHostPolicyConfigTest {

    @Mock
    ApplicationParams applicationParams;

    private final OverrideHostPolicyConfig config = new OverrideHostPolicyConfig(applicationParams);

    @Test
    void strictOverrideHostPolicy_shouldAllowValidUrls() {
        OverrideHostPolicy policy = config.strictOverrideHostPolicy();
        assertTrue(policy.isAllowed("https://service.aat.platform.hmcts.net"));
        assertTrue(policy.isAllowed("https://subdomain.demo.platform.hmcts.net:443/path"));
        assertTrue(policy.isAllowed("https://aat.platform.hmcts.net"));
        assertTrue(policy.isAllowed("https://sub1.sub2.preview.platform.hmcts.net:12345/abc"));
    }

    @Test
    void strictOverrideHostPolicy_shouldRejectInvalidUrls() {
        OverrideHostPolicy policy = config.strictOverrideHostPolicy();
        assertFalse(policy.isAllowed("http://service.aat.platform.hmcts.net")); // not https
        assertFalse(policy.isAllowed("https://service.prod.platform.hmcts.net")); // not allowed env
        assertFalse(policy.isAllowed("https://service.aat.platform.hmcts.com")); // wrong domain
        assertFalse(policy.isAllowed("https://service.aat.platform.hmcts.net.evil.com")); // subdomain attack
        assertFalse(policy.isAllowed(null));
        assertFalse(policy.isAllowed(""));
    }

    @Test
    void permissiveOverrideHostPolicy_shouldAllowAnyNonEmptyUrl() {
        OverrideHostPolicy policy = config.permissiveOverrideHostPolicy();
        assertTrue(policy.isAllowed("anything"));
        assertTrue(policy.isAllowed("https://any.url"));
        assertFalse(policy.isAllowed(null));
        assertFalse(policy.isAllowed("   "));
    }
}
