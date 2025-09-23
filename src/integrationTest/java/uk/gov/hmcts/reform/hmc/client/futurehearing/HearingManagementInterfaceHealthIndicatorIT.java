package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
    private static final String KEY_API_ERROR_MESSAGE = "apiErrorMessage";

    private static final String MESSAGE_MISSING_INVALID_PARAMS = "Missing or invalid request parameters";
    private static final String MESSAGE_SERVER_ERROR = "Server error";

    private static final String API_NAME_AD = "ActiveDirectory";
    private static final String API_NAME_HMI = "HearingManagementInterface";

    private final HearingManagementInterfaceHealthIndicator hmiHealthIndicator;

    @Autowired
    public HearingManagementInterfaceHealthIndicatorIT(HearingManagementInterfaceHealthIndicator hmiHealthIndicator) {
        this.hmiHealthIndicator = hmiHealthIndicator;
    }

    @ParameterizedTest
    @MethodSource("healthStatuses")
    void healthShouldMatchHealthCheckStatus(Status healthStatus) {
        stubHealthCheck(AUTH_TOKEN, healthStatus);

        Health health = hmiHealthIndicator.health();

        assertEquals(healthStatus, health.getStatus(), "Health status has unexpected value");
    }

    @ParameterizedTest
    @MethodSource("activeDirectoryApiErrors")
    void healthShouldBeDownForActiveDirectoryApiErrors(int responseStatusCode,
                                                       String responseErrorDescription,
                                                       List<Integer> responseErrorCodes,
                                                       Map<String, Object> expectedDetails) {
        stubFailToReturnToken(responseStatusCode, responseErrorDescription, responseErrorCodes);

        Health health = hmiHealthIndicator.health();

        assertHealthDown(health, expectedDetails);

        removeStubFailToReturnToken();
    }

    @Test
    void healthShouldBeDownForActiveDirectoryTimeout() {
        // Needs to be used in conjunction with the readTimeout property set in the TestPropertySource annotation.
        // Note that when openfeign is updated to v4.x the property name will need to be changed to
        // spring.cloud.openfeign.client.config.future-hearing-api.readTimeout
        stubFailToReturnTokenTimeout();

        Health health = hmiHealthIndicator.health();

        Map<String, Object> expectedDetails = Map.of(KEY_MESSAGE, "Connection/Read timeout",
                                                     KEY_API_NAME, API_NAME_AD);
        assertHealthDown(health, expectedDetails);

        removeStubFailToReturnTokenTimeout();
    }

    @ParameterizedTest
    @MethodSource("hmiApiErrors")
    void healthShouldBeDownForHmiApiErrors(int responseStatusCode,
                                           String responseMessage,
                                           Map<String, Object> expectedDetails) {
        stubHealthCheckThrowingError(AUTH_TOKEN, responseStatusCode, responseMessage);

        Health health = hmiHealthIndicator.health();

        assertHealthDown(health, expectedDetails);
    }

    private static Stream<Arguments> healthStatuses() {
        return Stream.of(
            arguments(Status.UP),
            arguments(Status.DOWN),
            arguments(Status.OUT_OF_SERVICE),
            arguments(Status.UNKNOWN)
        );
    }

    private static Stream<Arguments> activeDirectoryApiErrors() {
        String errorMessage = """
            {
                "error_description":"An AD API error",
                "error_codes":[1000]
            }""";

        return Stream.of(
            arguments(400, "An AD API error", List.of(1000), Map.of(KEY_MESSAGE, MESSAGE_MISSING_INVALID_PARAMS,
                                                                    KEY_API_NAME, API_NAME_AD,
                                                                    KEY_API_STATUS_CODE, 400,
                                                                    KEY_API_ERROR_MESSAGE, errorMessage)
            ),
            arguments(500, "Another AD API error", List.of(2000), Map.of(KEY_MESSAGE, MESSAGE_SERVER_ERROR,
                                                                         KEY_API_NAME, API_NAME_AD)
            )
        );
    }

    private static Stream<Arguments> hmiApiErrors() {
        String errorMessage = """
            {
                "statusCode": 401,
                "message": "An HMI API error"
            }""";

        return Stream.of(
            arguments(401, "An HMI API error", Map.of(KEY_MESSAGE, MESSAGE_MISSING_INVALID_PARAMS,
                                                      KEY_API_NAME, API_NAME_HMI,
                                                      KEY_API_STATUS_CODE, 401,
                                                      KEY_API_ERROR_MESSAGE, errorMessage)
            ),
            arguments(500, "Another HMI API error", Map.of(KEY_MESSAGE, MESSAGE_SERVER_ERROR,
                                                           KEY_API_NAME, API_NAME_HMI)
            )
        );
    }

    private void assertHealthDown(Health health, Map<String, Object> expectedHealthDetails) {
        assertNotNull(health, "Health should not be null");
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN");

        Map<String, Object> actualHealthDetails = health.getDetails();
        assertNotNull(actualHealthDetails, "Health details should not be null");
        assertEquals(expectedHealthDetails.size(),
                     actualHealthDetails.size(),
                     "Health details does not contain expected number of items");

        for (String key : expectedHealthDetails.keySet()) {
            assertTrue(actualHealthDetails.containsKey(key),
                       "Health details should contain an entry for '" + key + "'");
            assertEquals(expectedHealthDetails.get(key),
                         actualHealthDetails.get(key),
                         "Details entry for '" + key + "' has unexpected value");
        }
    }
}
