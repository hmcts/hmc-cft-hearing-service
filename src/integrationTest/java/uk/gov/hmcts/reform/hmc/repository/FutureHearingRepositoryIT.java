package uk.gov.hmcts.reform.hmc.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HealthCheckResponse;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckActiveDirectoryException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckHmiException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.removeStubFailToReturnToken;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.removeStubFailToReturnTokenTimeout;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteMethodThrowingError;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubFailToReturnToken;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubFailToReturnTokenTimeout;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubHealthCheck;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubHealthCheckThrowingError;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubUpdateMethodThrowingError;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;

// Set future-hearing-api read timeout value to force a timeout during AD API timeout test.
@TestPropertySource(
    properties = {
        "spring.cloud.openfeign.client.config.future-hearing-api.readTimeout = 10"
    }
)
class FutureHearingRepositoryIT extends BaseTest {

    private static final String TOKEN = "example-token";
    private static final String HMI_REQUEST_URL = "/resources/linked-hearing-group";
    private static final String REQUEST_ID = "12345";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";
    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();
    private static final JsonNode data = OBJECT_MAPPER.convertValue("Test data", JsonNode.class);

    private static final String API_NAME_AD = "ActiveDirectory";

    private final DefaultFutureHearingRepository defaultFutureHearingRepository;

    @Autowired
    public FutureHearingRepositoryIT(DefaultFutureHearingRepository defaultFutureHearingRepository) {
        this.defaultFutureHearingRepository = defaultFutureHearingRepository;
    }

    @Nested
    @DisplayName("Delete Linked Hearing Group")
    class DeleteLinkedHearingGroup {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldSuccessfullyDeleteLinkedHearingGroup() {
            stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, REQUEST_ID);
            defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow400AuthenticationException() {
            stubDeleteMethodThrowingError(400, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID))
                .isInstanceOf(BadFutureHearingRequestException.class)
                .hasMessageContaining(INVALID_REQUEST);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow500AuthenticationException() {
            stubDeleteMethodThrowingError(500, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID))
                .isInstanceOf(FutureHearingServerException.class)
                .hasMessageContaining(SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("Update Linked Hearing Group")
    class UpdateLinkedHearingGroup {
        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow400AuthenticationExceptionForPut() {
            stubUpdateMethodThrowingError(400, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.updateLinkedHearingGroup(REQUEST_ID, data))
                .isInstanceOf(BadFutureHearingRequestException.class)
                .hasMessageContaining(INVALID_REQUEST);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow500AuthenticationExceptionForPut() {
            stubUpdateMethodThrowingError(500, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.updateLinkedHearingGroup(REQUEST_ID, data))
                .isInstanceOf(FutureHearingServerException.class)
                .hasMessageContaining(SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("Private Health Check")
    class PrivateHealthCheck {

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#healthStatuses")
        void shouldSuccessfullyGetHealthStatus(Status healthStatus) {
            stubHealthCheck(TOKEN, healthStatus);

            HealthCheckResponse response = defaultFutureHearingRepository.privateHealthCheck();

            assertNotNull(response, "Health check response should not be null");
            assertEquals(healthStatus, response.getStatus(), "Health check response has unexpected health status");
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#adApiErrorsAndExpectedHealthCheckValues")
        void shouldThrowHealthCheckActiveDirectoryExceptionForAdApiErrors(Integer responseStatusCode,
                                                                          String responseErrorDescription,
                                                                          List<Integer> responseErrorCodes,
                                                                          String apiName,
                                                                          String message,
                                                                          Integer statusCode,
                                                                          String errorResponse) {
            stubFailToReturnToken(responseStatusCode, responseErrorDescription, responseErrorCodes);

            HealthCheckActiveDirectoryException actualException =
                assertThrows(HealthCheckActiveDirectoryException.class,
                             defaultFutureHearingRepository::privateHealthCheck);

            assertHealthCheckException(apiName, message, statusCode, errorResponse, actualException);

            removeStubFailToReturnToken();
        }

        @Test
        void shouldThrowHealthCheckActiveDirectoryExceptionForAdApiTimeout() {
            // Needs to be used in conjunction with the readTimeout property set in the TestPropertySource annotation.
            stubFailToReturnTokenTimeout();

            HealthCheckActiveDirectoryException actualException =
                assertThrows(HealthCheckActiveDirectoryException.class,
                             defaultFutureHearingRepository::privateHealthCheck);

            assertHealthCheckException(API_NAME_AD, "Connection/Read timeout", null, null, actualException);

            removeStubFailToReturnTokenTimeout();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.hmc.utils.TestingUtil#hmiApiErrorsAndExpectedHealthCheckValues")
        void shouldThrowHealthCheckHmiExceptionForHmiApiErrors(Integer responseStatusCode,
                                                               String responseMessage,
                                                               String apiName,
                                                               String message,
                                                               Integer statusCode,
                                                               String errorResponse) {
            stubHealthCheckThrowingError(TOKEN, responseStatusCode, responseMessage);

            HealthCheckHmiException actualException =
                assertThrows(HealthCheckHmiException.class,
                             defaultFutureHearingRepository::privateHealthCheck);

            assertHealthCheckException(apiName, message, statusCode, errorResponse, actualException);
        }

        private void assertHealthCheckException(String apiName,
                                                String message,
                                                Integer statusCode,
                                                String errorResponse,
                                                HealthCheckException actualException) {
            assertEquals(apiName, actualException.getApiName(), "Health check exception has unexpected API name");

            assertEquals(message, actualException.getMessage(), "Health check exception has unexpected message");

            if (statusCode == null) {
                assertNull(statusCode, "Health check exception status code should be null");
            } else {
                assertEquals(statusCode,
                             actualException.getStatusCode(),
                             "Health check exception has unexpected status code");
            }
            if (errorResponse == null) {
                assertNull(actualException.getErrorResponse(), "Health check exception error response should be null");
            } else {
                assertEquals(errorResponse,
                             actualException.getErrorResponse(),
                             "Health check exception has unexpected error response");
            }
        }
    }
}
