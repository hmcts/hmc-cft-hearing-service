package uk.gov.hmcts.reform.hmc.client.futurehearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckActiveDirectoryException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckHmiException;
import uk.gov.hmcts.reform.hmc.repository.FutureHearingRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingManagementInterfaceHealthIndicatorTest {

    private static final String DETAILS_KEY_MESSAGE = "message";
    private static final String DETAILS_KEY_API_NAME = "apiName";
    private static final String DETAILS_KEY_API_STATUS_CODE = "apiStatusCode";
    private static final String DETAILS_KEY_API_ERROR_RESPONSE = "apiErrorResponse";

    private static final String EXCEPTION_MESSAGE_AD = "AD exception message";
    private static final String ERROR_RESPONSE_AD = "AD error response";
    private static final String EXCEPTION_MESSAGE_HMI = "HMI exception message";
    private static final String ERROR_RESPONSE_HMI = "HMI error response";

    private static final String API_NAME_AD = "ActiveDirectory";
    private static final String API_NAME_HMI = "HearingManagementInterface";

    @Mock
    private FutureHearingRepository futureHearingRepository;

    private HearingManagementInterfaceHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new HearingManagementInterfaceHealthIndicator(futureHearingRepository);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#healthStatuses")
    void healthShouldMatchHealthCheckStatus(Status healthStatus) {
        HealthCheckResponse response = new HealthCheckResponse();
        response.setStatus(healthStatus);

        when(futureHearingRepository.privateHealthCheck()).thenReturn(response);

        Health health = healthIndicator.health();
        assertNotNull(health, "Health should not be null");
        assertEquals(healthStatus, health.getStatus(), "Health has unexpected health status");

        verify(futureHearingRepository).privateHealthCheck();
    }

    @ParameterizedTest
    @MethodSource("healthCheckExceptions")
    void healthShouldBeDownForHealthCheckExceptions(HealthCheckException healthCheckException,
                                                    List<ExpectedDetail> expectedDetails) {
        when(futureHearingRepository.privateHealthCheck()).thenThrow(healthCheckException);

        Health health = healthIndicator.health();
        assertNotNull(health, "Health should not be null");
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN");

        Map<String, Object> details = health.getDetails();
        assertEquals(expectedDetails.size(), details.size(), "Health details has unexpected number of items");

        for (ExpectedDetail expectedDetail : expectedDetails) {
            assertTrue(details.containsKey(expectedDetail.key),
                       "Health details should contain an item with key '" + expectedDetail.key + "'");
            assertEquals(expectedDetail.value,
                         details.get(expectedDetail.key),
                         "Health details has unexpected value for key '" + expectedDetail.key + "'");
        }

        verify(futureHearingRepository).privateHealthCheck();
    }

    @Test
    void healthShouldBeDownForNonHealthCheckExceptions() {
        RuntimeException runtimeException = new RuntimeException("Runtime exception message");

        when(futureHearingRepository.privateHealthCheck()).thenThrow(runtimeException);

        Health health = healthIndicator.health();
        assertNotNull(health, "Health should not be null");
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN");

        Map<String, Object> details = health.getDetails();
        assertEquals(1, details.size(), "Health details has unexpected number of items");
        assertTrue(details.containsKey(DETAILS_KEY_MESSAGE),
                   "Health details should contain an item with key 'message'");
        assertEquals("Runtime exception message",
                     details.get(DETAILS_KEY_MESSAGE),
                     "Health details has unexpected value for key 'message'");

        verify(futureHearingRepository).privateHealthCheck();
    }

    private static Stream<Arguments> healthCheckExceptions() {
        return Stream.of(
            arguments(new HealthCheckActiveDirectoryException(EXCEPTION_MESSAGE_AD),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_AD),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_AD)
                      )
            ),
            arguments(new HealthCheckActiveDirectoryException(EXCEPTION_MESSAGE_AD, null, null),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_AD),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_AD)
                      )
            ),
            arguments(new HealthCheckActiveDirectoryException(EXCEPTION_MESSAGE_AD, 400, null),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_AD),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_AD),
                              new ExpectedDetail(DETAILS_KEY_API_STATUS_CODE, 400)
                      )
            ),
            arguments(new HealthCheckActiveDirectoryException(EXCEPTION_MESSAGE_AD, null, ERROR_RESPONSE_AD),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_AD),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_AD),
                              new ExpectedDetail(DETAILS_KEY_API_ERROR_RESPONSE, ERROR_RESPONSE_AD)
                      )
            ),
            arguments(new HealthCheckActiveDirectoryException(EXCEPTION_MESSAGE_AD, 400, ERROR_RESPONSE_AD),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_AD),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_AD),
                              new ExpectedDetail(DETAILS_KEY_API_STATUS_CODE, 400),
                              new ExpectedDetail(DETAILS_KEY_API_ERROR_RESPONSE, ERROR_RESPONSE_AD)
                      )
            ),
            arguments(new HealthCheckHmiException(EXCEPTION_MESSAGE_HMI),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_HMI)
                      )
            ),
            arguments(new HealthCheckHmiException(EXCEPTION_MESSAGE_HMI, null, null),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_HMI)
                      )
            ),
            arguments(new HealthCheckHmiException(EXCEPTION_MESSAGE_HMI, 401, null),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_STATUS_CODE, 401)
                      )
            ),
            arguments(new HealthCheckHmiException(EXCEPTION_MESSAGE_HMI, null, ERROR_RESPONSE_HMI),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_ERROR_RESPONSE, ERROR_RESPONSE_HMI)
                      )
            ),
            arguments(new HealthCheckHmiException(EXCEPTION_MESSAGE_HMI, 401, ERROR_RESPONSE_HMI),
                      List.of(new ExpectedDetail(DETAILS_KEY_MESSAGE, EXCEPTION_MESSAGE_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_NAME, API_NAME_HMI),
                              new ExpectedDetail(DETAILS_KEY_API_STATUS_CODE, 401),
                              new ExpectedDetail(DETAILS_KEY_API_ERROR_RESPONSE, ERROR_RESPONSE_HMI)
                      )
            )
        );
    }

    private record ExpectedDetail(String key, Object value) {}
}
