package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideUrlValidatorTest {

    private final OverrideUrlValidator validator = new OverrideUrlValidator();

    @ParameterizedTest(name = "allows valid HMCTS override url: {0}")
    @ValueSource(strings = {
        "https://ccd-data-store-api-test-case-api-pr-XXX.preview.platform.hmcts.net",
        "https://am-role-assignment-test-case-api-pr-XXX.preview.platform.hmcts.net",
        "https://service.aat.platform.hmcts.net",
        "https://service.demo.platform.hmcts.net",
        "https://foo-bar.preview.platform.hmcts.net"
    })
    void shouldAllowValidOverrideUrls(String url) {
        assertTrue(validator.isAllowed(url));
    }

    @ParameterizedTest(name = "rejects invalid override url: {0}")
    @ValueSource(strings = {
        "http://ccd-data-store-api-test-case-api-pr-XXX.preview.platform.hmcts.net",
        "https://abc.com",
        "https://preview.platform.hmcts.net.abc.com",
        "https://service.prod.platform.hmcts.net",
        "https://169.254.111.111/latest/meta-data",
        "not-a-url",
        "   ",
        ""
    })
    void shouldRejectInvalidOverrideUrls(String url) {
        assertFalse(validator.isAllowed(url));
    }

    @Test
    @DisplayName("null is rejected")
    void shouldRejectNull() {
        assertFalse(validator.isAllowed(null));
    }
}
