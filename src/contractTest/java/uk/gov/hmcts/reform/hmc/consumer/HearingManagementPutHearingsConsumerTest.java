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
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementPutHearingsConsumerTest extends BasePactTesting {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    private static final String PATH_HEARING = "/hearing";

    // Test data 1 - valid HearingRequest
    UpdateHearingRequest validHearingRequest = generateUpdateHearingRequest(VALID_CASE_REF);
    String jsonstringValidRequest = toJsonString(validHearingRequest);

    // Test data 2 - invalid HearingRequest
    HearingRequest invalidHearingRequest = generateInvalidHearingRequest();
    String jsonstringInvalidRequest = toJsonString(invalidHearingRequest);


    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );


    /**
     * update Hearing - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact updateHearing(PactDslWithProvider builder) {

        return builder
                .given(PROVIDER_NAME + " successfully returns UPDATED hearing")
                .uponReceiving("Request to UPDATE hearing")
                .path(PATH_HEARING)
                .method(HttpMethod.POST.toString())
                .body(jsonstringValidRequest, ContentType.APPLICATION_JSON)
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.ACCEPTED.value())
                .body(HearingResponsePactUtil.generateCreateHearingByPutJsonBody(MSG_200_UPDATE_HEARING,
                        "1", "HEARING_REQUESTED",
                         validHearingRequest.getCaseDetails().getRequestTimeStamp(),
                        validHearingRequest.getRequestDetails().getVersionNumber()))
                .toPact();
    }

    /**
     * validation error from update Hearing - send faulty request.
     *
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromUpdatingHearing(PactDslWithProvider builder) {
        return builder
                .given(PROVIDER_NAME + " throws validation error for UPDATE hearing")
                .uponReceiving("Request to UPDATE hearing")
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
     * test expects to return the updated hearing.
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "updateHearing")
    public void shouldReturnUpdatedHearing(MockServer mockServer) {
        JsonPath response = getRestAssuredJsonPath(mockServer);

        assertThat(response.getString("hearingRequestID"))
                .isNotEmpty();
        assertThat(response.getString("status"))
                .isNotEmpty();
        assertThat(response.getString("versionNumber"))
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
    @PactTestFor(pactMethod = "validationErrorFromUpdatingHearing")
    public void shouldReturn400BadRequestForUpdateHearing(MockServer mockServer) {
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
