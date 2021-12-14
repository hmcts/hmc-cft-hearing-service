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
public class HearingManagementDeleteHearingConsumerTest extends BasePactTesting {

    private static final String PATH_HEARING = "/hearing";

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
     * delete Hearing - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact deleteHearing(PactDslWithProvider builder) {
        return builder
            .given(PROVIDER_NAME + " successfully deletes hearing")
            .uponReceiving("Request to DELETE hearing with valid hearing id")
            .path(PATH_HEARING)
            .query(FIELD_HEARING_ID + "=" + TEST_HEARIMG_ID)
            .method(HttpMethod.DELETE.toString())
            .body(jsonValidHearingRequest, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(HearingResponsePactUtil.generateCreateHearingByPutJsonBody(
                HearingManagementController.MSG_200_DELETE_HEARING))
            .toPact();
    }

    /**
     * validation error from delete Hearing - send faulty request.
     *
     * @param builder builder object
     * @return response RequestResponsePact
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder
            .given(PROVIDER_NAME + " throws validation error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for invalid hearing request")
            .path(PATH_HEARING)
            .query(FIELD_HEARING_ID + "=" + TEST_HEARIMG_ID)
            .method(HttpMethod.DELETE.toString())
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
     * test expects to return the deleted hearing.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "deleteHearing")
    public void shouldReturn202DeletedHearing(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(FIELD_HEARING_ID, TEST_HEARIMG_ID)
            .body(jsonCreatedHearingResponse(validHearingRequest))
            .when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
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
    @PactTestFor(pactMethod = "validationErrorFromDeleteHearing")
    public void shouldReturn400BadRequestForDeleteHearing(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(FIELD_HEARING_ID, TEST_HEARIMG_ID)
            .body(jsonInvalidHearingRequest)
            .when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

}
