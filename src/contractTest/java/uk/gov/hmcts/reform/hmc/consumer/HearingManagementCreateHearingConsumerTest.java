package uk.gov.hmcts.reform.hmc.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.controllers.HearingManagementController;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementCreateHearingConsumerTest extends BasePactTesting {

    private static final String PATH_HEARING = "/hearing";
    private static final String BROKER_SERVICE_NAME = "hmc cftHearingService";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ERRORS = "errors";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String TEST_HEARIMG_ID = "2000000001";

    // Test data 1 - valid HearingRequest
    HearingRequest validHearingRequest = generateHearingRequest(VALID_CASE_REF);
    String jsonValidHearingRequest = jsonCreatedHearingResponse(validHearingRequest);

    // Test data 2 - invalid HearingRequest
    HearingRequest invalidHearingRequest = generateInvalidHearingRequest();
    String jsonInvalidHearingRequest = jsonCreatedHearingResponse(invalidHearingRequest);

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * create Hearing by POST - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact createHearingByPost(PactDslWithProvider builder) {
        return builder
            .given(BROKER_SERVICE_NAME + " successfully returns created hearing")
            .uponReceiving("Request to create hearing by POST with valid hearing request")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonValidHearingRequest, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(HearingResponsePactUtil.generateCreateHearingByPostJsonBody(
                HearingManagementController.MSG_202_CREATE_HEARING))
            .toPact();
    }

    /**
     * create Hearing by PUT - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact createHearingByPut(PactDslWithProvider builder) {
        return builder
            .given(BROKER_SERVICE_NAME + " successfully returns created hearing")
            .uponReceiving("Request to create hearing by PUT with valid hearing request")
            .path(PATH_HEARING)
            .query(FIELD_HEARING_ID + "=" + TEST_HEARIMG_ID)
            .method(HttpMethod.PUT.toString())
            .body(jsonValidHearingRequest, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(HearingResponsePactUtil.generateCreateHearingByPutJsonBody(
                HearingManagementController.MSG_202_CREATE_HEARING))
            .toPact();
    }

    /**
     * validation error from create Hearing by POST - send faulty request.
     *
     * @param builder builder object
     * @return response RequestResponsePact
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromCreateHearingByPost(PactDslWithProvider builder) {
        return builder
            .given(BROKER_SERVICE_NAME + " throws validation error while trying to create hearing")
            .uponReceiving("Request to create hearing by POST for invalid hearing request")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonInvalidHearingRequest, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, HearingManagementController.MSG_400_CREATE_HEARING)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray()
            )
            .toPact();
    }

    /**
     * validation error from create Hearing by PUT - send faulty request.
     *
     * @param builder builder object
     * @return response RequestResponsePact
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromCreateHearingByPut(PactDslWithProvider builder) {
        return builder
            .given(BROKER_SERVICE_NAME + " throws validation error while trying to create hearing by PUT")
            .uponReceiving("Request to create hearing by PUT for invalid hearing request")
            .path(PATH_HEARING)
            .query(FIELD_HEARING_ID + "=" + TEST_HEARIMG_ID)
            .method(HttpMethod.PUT.toString())
            .body(jsonInvalidHearingRequest, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, HearingManagementController.MSG_400_CREATE_HEARING)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray()
            )
            .toPact();
    }

    /**
     * test expects to return the created hearing by POST.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "createHearingByPost")
    public void shouldReturn202CreatedHearingByPost(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(jsonCreatedHearingResponse(validHearingRequest))
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

    /**
     * test expects to return the created hearing by PUT.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "createHearingByPut")
    public void shouldReturn202CreatedHearingByPut(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(FIELD_HEARING_ID, TEST_HEARIMG_ID)
            .body(jsonCreatedHearingResponse(validHearingRequest))
            .when()
            .put(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

    /**
     * test expects an error 400 on create Hearing by Post.
     *
     * @param mockServer MockServer object
     */
    @Test
    @PactTestFor(pactMethod = "validationErrorFromCreateHearingByPost")
    public void shouldReturn400BadRequestForCreateHearingByPost(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(jsonInvalidHearingRequest)
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

    /**
     * test expects to return an error 400 on create Hearing by Put.
     *
     * @param mockServer MockServer object
     */
    @Test
    @PactTestFor(pactMethod = "validationErrorFromCreateHearingByPut")
    public void shouldReturn400BadRequestForCreateHearingByPut(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(FIELD_HEARING_ID, TEST_HEARIMG_ID)
            .body(jsonInvalidHearingRequest)
            .when()
            .put(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

}
