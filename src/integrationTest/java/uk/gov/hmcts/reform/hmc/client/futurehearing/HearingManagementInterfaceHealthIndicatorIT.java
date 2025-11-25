package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.removeStubFailToReturnToken;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.removeStubFailToReturnTokenTimeout;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubFailToReturnToken;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubFailToReturnTokenTimeout;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubHealthCheck;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubHealthCheckThrowingError;

// Set future-hearing-api read timeout value to force a timeout during AD API timeout test.
@TestPropertySource(
    properties = {
        "feign.client.config.future-hearing-api.readTimeout = 10"
    }
)
class HearingManagementInterfaceHealthIndicatorIT extends BaseTest {

    private static final String AUTH_TOKEN = "example-token";

    private static final String KEY_MESSAGE = "message";
    private static final String KEY_API_NAME = "apiName";
    private static final String KEY_API_STATUS_CODE = "apiStatusCode";
    private static final String KEY_API_ERROR_RESPONSE = "apiErrorResponse";

    private static final String API_NAME_AD = "ActiveDirectory";

    private final HearingManagementInterfaceHealthIndicator hmiHealthIndicator;

    @Autowired
    public HearingManagementInterfaceHealthIndicatorIT(HearingManagementInterfaceHealthIndicator hmiHealthIndicator) {
        this.hmiHealthIndicator = hmiHealthIndicator;
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#healthStatuses")
    void healthShouldMatchHealthCheckStatus(Status healthStatus) {
        stubHealthCheck(AUTH_TOKEN, healthStatus);

        Health health = hmiHealthIndicator.health();

        assertEquals(healthStatus, health.getStatus(), "Health status has unexpected value");
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#adApiErrorsAndExpectedHealthCheckValues")
    void healthShouldBeDownForActiveDirectoryApiErrors(int responseStatusCode,
                                                       String responseErrorDescription,
                                                       List<Integer> responseErrorCodes,
                                                       String apiName,
                                                       String message,
                                                       Integer statusCode,
                                                       String errorResponse) {
        stubFailToReturnToken(responseStatusCode, responseErrorDescription, responseErrorCodes);

        Health health = hmiHealthIndicator.health();

        assertHealthDown(health, apiName, message, statusCode, errorResponse);

        removeStubFailToReturnToken();
    }

    @Test
    void healthShouldBeDownForActiveDirectoryTimeout() {
        // Needs to be used in conjunction with the readTimeout property set in the TestPropertySource annotation.
        // Note that when openfeign is updated to v4.x the property name will need to be changed to
        // spring.cloud.openfeign.client.config.future-hearing-api.readTimeout
        stubFailToReturnTokenTimeout();

        Health health = hmiHealthIndicator.health();

        assertHealthDown(health, API_NAME_AD, "Connection/Read timeout", null, null);

        removeStubFailToReturnTokenTimeout();
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#hmiApiErrorsAndExpectedHealthCheckValues")
    void healthShouldBeDownForHmiApiErrors(int responseStatusCode,
                                           String responseMessage,
                                           String apiName,
                                           String message,
                                           Integer statusCode,
                                           String errorResponse) {
        stubHealthCheckThrowingError(AUTH_TOKEN, responseStatusCode, responseMessage);

        Health health = hmiHealthIndicator.health();

        assertHealthDown(health, apiName, message, statusCode, errorResponse);
    }

    private void assertHealthDown(Health health,
                                  String apiName,
                                  String message,
                                  Integer statusCode,
                                  String errorResponse) {
        assertNotNull(health, "Health should not be null");
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN");

        Map<String, Object> actualHealthDetails = health.getDetails();
        assertNotNull(actualHealthDetails, "Health details should not be null");

        assertHealthEntry(actualHealthDetails, KEY_API_NAME, apiName);
        assertHealthEntry(actualHealthDetails, KEY_MESSAGE, message);
        assertHealthEntry(actualHealthDetails, KEY_API_STATUS_CODE, statusCode);
        assertHealthEntry(actualHealthDetails, KEY_API_ERROR_RESPONSE, errorResponse);
    }

    private void assertHealthEntry(Map<String, Object> healthDetails, String key, Object value) {
        if (value == null) {
            assertFalse(healthDetails.containsKey(key), "Health details should not contain an entry for '" + key + "'");
        } else {
            assertTrue(healthDetails.containsKey(key), "Health details should contain an entry for '" + key + "'");
            assertEquals(value, healthDetails.get(key), "Health details entry for '" + key + "' has unexpected value");
        }
    }
}
