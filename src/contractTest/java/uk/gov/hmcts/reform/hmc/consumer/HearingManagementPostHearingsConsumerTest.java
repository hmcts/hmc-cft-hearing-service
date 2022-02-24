package uk.gov.hmcts.reform.hmc.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementPostHearingsConsumerTest extends BasePactTesting {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    private static final String PATH_HEARING = "/hearing";

    // Test data 1 - valid HearingRequest
    HearingRequest validHearingRequest = generateHearingRequest(VALID_CASE_REF);
    String jsonstringValidRequest = toJsonString(validHearingRequest);

    // Test data 2 - invalid HearingRequest
    HearingRequest invalidHearingRequest = generateInvalidHearingRequest();
    String jsonstringInvalidRequest = toJsonString(invalidHearingRequest);


    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );


    /**
     * create Hearing with Individual Details - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact createHearing(PactDslWithProvider builder) {

        return builder
                .given(PROVIDER_NAME + " successfully returns created hearing")
                .uponReceiving("Request to CREATE hearing")
                .path(PATH_HEARING)
                .method(HttpMethod.POST.toString())
                .body(jsonstringValidRequest, ContentType.APPLICATION_JSON)
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.ACCEPTED.value())
                .body(HearingResponsePactUtil.generateCreateHearingByPostJsonBody(MSG_200_CREATE_HEARING,
                        "1", "HEARING_REQUESTED", LocalDateTime.now()))
                .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     *
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromCreatingHearing(PactDslWithProvider builder) {
        return builder
                .given(PROVIDER_NAME + " throws validation error for CREATE hearing")
                .uponReceiving("Request to CREATE hearing")
                .path(PATH_HEARING)
                .method(HttpMethod.POST.toString())
                .body(jsonstringInvalidRequest, ContentType.APPLICATION_JSON)
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.BAD_REQUEST.value())
                .body(new PactDslJsonBody()
                        .stringType("message", "Invalid hearing details")
                        .stringValue("status", "BAD_REQUEST")
                        .eachLike("errors", 1)
                        .closeArray()
                )
                .toPact();
    }

    /**
     * test expects to return the created hearing.
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "createHearing")
    public void shouldReturnCreatedHearing(MockServer mockServer) {
        JsonPath response = getRestAssuredJsonPath(mockServer);

        assertThat(response.getString("hearingRequestID"))
                .isNotEmpty();
        assertThat(response.getString("status"))
                .isNotEmpty();
        assertThat(response.getString("timeStamp"))
                .isNotEmpty();
    }

    /**
     * get RestAssuredJsonPath.
     *
     * @param mockServer MockServer
     */
    public JsonPath getRestAssuredJsonPath(MockServer mockServer) {
        return RestAssured
                .given()
                .headers(headers)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(toJsonString(validHearingRequest))
                .when()
                .post(mockServer.getUrl() + PATH_HEARING)
                .then()
                .statusCode(202)
                .and()
                .extract()
                .body()
                .jsonPath();
    }

    /**
     * test expects an error 400.
     *
     * @param mockServer MockServer object
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "validationErrorFromCreatingHearing")
    public void shouldReturn400BadRequestForCreateHearing(MockServer mockServer) {
        JsonPath response = RestAssured
                .given()
                .headers(headers)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(jsonstringInvalidRequest)
                .when()
                .post(mockServer.getUrl() + PATH_HEARING)
                .then()
                .statusCode(400)
                .and()
                .extract()
                .body()
                .jsonPath();

        assertThat(response.getString("message")).isEqualTo("Invalid hearing details");
        assertThat(response.getString("status")).isEqualTo("BAD_REQUEST");
    }

}
