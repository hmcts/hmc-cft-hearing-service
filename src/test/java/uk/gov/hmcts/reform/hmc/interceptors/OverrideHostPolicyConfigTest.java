package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class OverrideHostPolicyConfigTest {

    @Mock
    ApplicationParams applicationParams;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(applicationParams.getAllowedOverRideHostPatterns()).thenReturn(List.of(
            "^https://(?:[a-z0-9-]+\\.){0,5}(preview|aat|demo)\\.platform\\.hmcts\\.net(?::\\d{1,5})?(?:/.*)?$"
        ));
    }

    @Test
    void strictOverrideHostPolicy_shouldAllowValidUrls() {

        OverrideHostPolicy policy = new OverrideHostPolicyConfig(applicationParams).strictOverrideHostPolicy();
        assertTrue(policy.isAllowed("https://service.aat.platform.hmcts.net"));
        assertTrue(policy.isAllowed("https://subdomain.demo.platform.hmcts.net:443/path"));
        assertTrue(policy.isAllowed("https://aat.platform.hmcts.net"));
        assertTrue(policy.isAllowed("https://sub1.sub2.preview.platform.hmcts.net:12345/abc"));
    }

    @Test
    void strictOverrideHostPolicy_shouldRejectInvalidUrls() {
        OverrideHostPolicy policy = new OverrideHostPolicyConfig(applicationParams).strictOverrideHostPolicy();
        assertFalse(policy.isAllowed("http://service.aat.platform.hmcts.net")); // not https
        assertFalse(policy.isAllowed("https://service.prod.platform.hmcts.net")); // not allowed env
        assertFalse(policy.isAllowed("https://service.aat.platform.hmcts.com")); // wrong domain
        assertFalse(policy.isAllowed("https://service.aat.platform.hmcts.net.evil.com")); // subdomain attack
        assertFalse(policy.isAllowed(null));
        assertFalse(policy.isAllowed(""));
    }


}
