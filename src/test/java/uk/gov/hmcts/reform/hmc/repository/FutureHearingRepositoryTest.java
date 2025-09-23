package uk.gov.hmcts.reform.hmc.repository;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.futurehearing.ActiveDirectoryApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HealthCheckResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceApiClient;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceResponse;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckActiveDirectoryException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckException;
import uk.gov.hmcts.reform.hmc.exceptions.HealthCheckHmiException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static feign.Request.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class FutureHearingRepositoryTest {

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();

    private static final String ACCESS_TOKEN = "test-token";

    private AuthenticationResponse response;
    private String requestString;

    @InjectMocks
    private DefaultFutureHearingRepository repository;

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private ActiveDirectoryApiClient activeDirectoryApiClient;

    @Mock
    private HearingManagementInterfaceApiClient hmiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        response = new AuthenticationResponse();
        repository = new DefaultFutureHearingRepository(activeDirectoryApiClient, applicationParams, hmiClient);
        requestString = "grant_type=GRANT_TYPE&client_id=CLIENT_ID&scope=SCOPE&client_secret=CLIENT_SECRET";
        given(applicationParams.getGrantType()).willReturn("GRANT_TYPE");
        given(applicationParams.getClientId()).willReturn("CLIENT_ID");
        given(applicationParams.getScope()).willReturn("SCOPE");
        given(applicationParams.getClientSecret()).willReturn("CLIENT_SECRET");
    }

    @Test
    void shouldSuccessfullyReturnAuthenticationObject() {
        given(activeDirectoryApiClient.authenticate(requestString)).willReturn(response);
        AuthenticationResponse testResponse = repository.retrieveAuthToken();
        assertEquals(response, testResponse);
    }

    @Test
    void shouldSuccessfullyReturnHealthCheckResponse() {
        response.setAccessToken(ACCESS_TOKEN);
        given(activeDirectoryApiClient.authenticate(requestString)).willReturn(response);

        HealthCheckResponse healthCheckResponse = new HealthCheckResponse();
        healthCheckResponse.setStatus(Status.UP);
        given(hmiClient.privateHealthCheck("Bearer test-token")).willReturn(healthCheckResponse);

        HealthCheckResponse actualHealthCheckResponse = repository.privateHealthCheck();

        assertNotNull(actualHealthCheckResponse, "HealthCheckResponse should not be null");
        assertEquals(Status.UP, actualHealthCheckResponse.getStatus(), "HealthCheckResponse has unexpected status");

        then(activeDirectoryApiClient).should().authenticate(requestString);
        then(hmiClient).should().privateHealthCheck("Bearer test-token");
    }

    @ParameterizedTest
    @MethodSource("healthCheckActiveDirectoryApiExceptions")
    void healthCheckShouldThrowHealthCheckActiveDirectoryException(
        Exception activeDirectoryApiException, HealthCheckActiveDirectoryException expectedException) {
        given(activeDirectoryApiClient.authenticate(requestString)).willThrow(activeDirectoryApiException);

        HealthCheckActiveDirectoryException actualException =
            assertThrows(HealthCheckActiveDirectoryException.class,
                         () -> repository.privateHealthCheck(),
                         "HealthCheckActiveDirectoryException should be thrown");

        assertHealthCheckException(expectedException, actualException);

        then(activeDirectoryApiClient).should().authenticate(requestString);
    }

    @Test
    void healthCheckShouldLogRetryableExceptionWithNullRequestBody() {
        Logger logger = (Logger) LoggerFactory.getLogger(DefaultFutureHearingRepository.class);
        logger.setLevel(Level.DEBUG);

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        final Request getTokenRequest =
            Request.create(POST, "/get/token/url", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        RetryableException retryableException =
            new RetryableException(400, "Active Directory timeout", POST, null, null, getTokenRequest);

        given(activeDirectoryApiClient.authenticate(requestString)).willThrow(retryableException);

        HealthCheckActiveDirectoryException actualException =
            assertThrows(HealthCheckActiveDirectoryException.class,
                         () -> repository.privateHealthCheck(),
                         "HealthCheckActiveDirectoryException should be thrown");

        HealthCheckActiveDirectoryException expectedException =
            new HealthCheckActiveDirectoryException("Connection/Read timeout");
        assertHealthCheckException(expectedException, actualException);

        List<ILoggingEvent> logsList = listAppender.list;

        assertNotNull(logsList, "Log list should not be null");
        assertTrue(logsList.stream()
                       .anyMatch(log -> log.getLevel() == Level.DEBUG
                           && log.getFormattedMessage().equals("Request to Active Directory timed out - "
                                                                   + "URL: /get/token/url, Method: POST, Body: N/A")),
                   "Log list does not contain expected debug message");

        logger.detachAndStopAllAppenders();

        then(activeDirectoryApiClient).should().authenticate(requestString);
    }

    @ParameterizedTest
    @MethodSource("healthCheckHmiApiExceptions")
    void healthCheckShouldThrowHealthCheckHmiException(Exception hmiApiException,
                                                       HealthCheckHmiException expectedException) {
        response.setAccessToken(ACCESS_TOKEN);
        given(activeDirectoryApiClient.authenticate(requestString)).willReturn(response);

        given(hmiClient.privateHealthCheck("Bearer test-token")).willThrow(hmiApiException);

        HealthCheckHmiException actualException =
            assertThrows(HealthCheckHmiException.class,
                         () -> repository.privateHealthCheck(),
                         "HealthCheckHmiException should be thrown");

        assertHealthCheckException(expectedException, actualException);

        then(activeDirectoryApiClient).should().authenticate(requestString);
        then(hmiClient).should().privateHealthCheck("Bearer test-token");
    }

    @Test
    void shouldSuccessfullyCreateLinkHearingRequest() {
        HearingManagementInterfaceResponse expectedResponse = new HearingManagementInterfaceResponse();
        expectedResponse.setResponseCode(200);
        response.setAccessToken(ACCESS_TOKEN);
        given(activeDirectoryApiClient.authenticate(requestString)).willReturn(response);
        JsonNode anyData = OBJECT_MAPPER.convertValue("test data", JsonNode.class);
        repository.createLinkedHearingGroup(anyData);
        then(hmiClient).should().createLinkedHearingGroup("Bearer " + response.getAccessToken(), anyData);
    }

    private static Stream<Arguments> healthCheckActiveDirectoryApiExceptions() {
        byte[] requestBody = "DummyGetTokenRequestBody".getBytes(StandardCharsets.UTF_8);
        final Request getTokenRequest =
            Request.create(POST, "/get/token/url", Collections.emptyMap(), requestBody, StandardCharsets.UTF_8, null);

        return Stream.of(
            arguments(new BadFutureHearingRequestException("Active Directory bad request exception message",
                                                           400,
                                                           "Active Directory bad request error message"),
                      new HealthCheckActiveDirectoryException("Active Directory bad request exception message",
                                                              400,
                                                              "Active Directory bad request error message")
            ),
            arguments(new FutureHearingServerException("Active Directory server exception message"),
                      new HealthCheckActiveDirectoryException("Active Directory server exception message")
            ),
            arguments(new RetryableException(400, "Active Directory timeout", POST, null, null, getTokenRequest),
                      new HealthCheckActiveDirectoryException("Connection/Read timeout")
            )
        );
    }

    private static Stream<Arguments> healthCheckHmiApiExceptions() {
        return Stream.of(
            arguments(new BadFutureHearingRequestException("HMI bad request exception message",
                                                           401,
                                                           "HMI bad request error message"),
                      new HealthCheckHmiException("HMI bad request exception message",
                                                  401,
                                                  "HMI bad request error message")
            ),
            arguments(new FutureHearingServerException("HMI server exception message"),
                      new HealthCheckHmiException("HMI server exception message")
            )
        );
    }

    private void assertHealthCheckException(HealthCheckException expectedException,
                                            HealthCheckException actualException) {
        assertEquals(expectedException.getApiName(),
                     actualException.getApiName(),
                     "Health check exception has unexpected API name");

        assertEquals(expectedException.getMessage(),
                     actualException.getMessage(),
                     "Health check exception has unexpected message");

        if (expectedException.getStatusCode() != null) {
            assertEquals(expectedException.getStatusCode(),
                         actualException.getStatusCode(),
                         "Health check exception has unexpected status code");
        } else {
            assertNull(actualException.getStatusCode(),
                       "Health check exception status code should be null");
        }

        if (expectedException.getErrorMessage() != null) {
            assertEquals(expectedException.getErrorMessage(),
                         actualException.getErrorMessage(),
                         "Health check exception has unexpected error message");
        } else {
            assertNull(actualException.getErrorMessage(),
                       "Health check exception error message should be null");
        }
    }
}
