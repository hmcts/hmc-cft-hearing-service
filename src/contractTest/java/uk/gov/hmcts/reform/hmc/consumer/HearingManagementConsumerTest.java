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
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementConsumerTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    private static final String PATH_HEARING = "/hearing";

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * create Hearing - send valid request.
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "hmc", consumer = "hmc_hearing_consumer")
    public RequestResponsePact createHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("HMC successfully returns created hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(createValidCreateHearingRequest(), ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createCreateHearingResponse())
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = "hmc", consumer = "hmc_hearing_consumer")
    public RequestResponsePact validationErrorFromCreatingHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("HMC throws validation error for createHearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(createInvalidCreateHearingRequest(), ContentType.APPLICATION_JSON)
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
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "createHearing")
    public void shouldReturnCreatedHearing(MockServer mockServer) throws Exception {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(createValidCreateHearingRequest())
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .body()
            .jsonPath();

        assertThat(response.getString("status_message"))
            .isEqualTo("Hearing created successfully");
        assertThat(response.getString("requestDetails"))
            .isNotEmpty();
        assertThat(response.getString("hearingDetails"))
            .isNotEmpty();
        assertThat(response.getString("caseDetails"))
            .isNotEmpty();
        assertThat(response.getString("partyDetails"))
            .isNotEmpty();
    }

    /**
     * test expects an error 400.
     * @param mockServer MockServer object
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "validationErrorFromCreatingHearing")
    public void shouldReturn400BadRequestForCreateHearing(MockServer mockServer) throws Exception {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(createInvalidCreateHearingRequest())
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

    /**
     * create the create Hearing Response.
     * @return PactJsonBody pact JSON body
     */
    private PactDslJsonBody createCreateHearingResponse() {
        HearingRequest hearingRequest = TestingUtil.getHearingRequest();
        return new PactDslJsonBody()
            .stringType("status_message", "Hearing created successfully")
            .stringType("requestDetails", toRequestDetailsJsonString(hearingRequest))
            .stringType("hearingDetails", toHearingDetailsJsonString(hearingRequest))
            .stringType("caseDetails", toCaseDetailsJsonString(hearingRequest))
            .stringType("partyDetails", toPartyDetailsJsonString(hearingRequest));
    }

    /**
     * create a Valid Create Hearing Request.
     * @return String JSON body
     */
    private String createValidCreateHearingRequest() {
        HearingRequest hearingRequest = TestingUtil.getHearingRequest();
        return toHearingRequestJsonString(hearingRequest);
    }

    /**
     * create an Invalid Create Hearing Request.
     * @return String JSON body
     */
    private String createInvalidCreateHearingRequest() {
        HearingRequest hearingRequest = TestingUtil.getHearingRequest();
        hearingRequest.setCaseDetails(null);
        return toHearingRequestJsonString(hearingRequest);
    }

    /**
     * get JSON String from hearing Request.
     * @return String JSON string of hearing Request
     */
    private String toHearingRequestJsonString(HearingRequest hearingRequest) {
        JSONObject jsonObject = new JSONObject(hearingRequest);
        return jsonObject.toString();
    }

    /**
     * get Request details JSON String from Hearing Request.
     * @return String JSON string of Request details
     */
    private String toRequestDetailsJsonString(HearingRequest hearingRequest) {
        if (null != hearingRequest && null != hearingRequest.getRequestDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getRequestDetails());
            return jsonObject.toString();
        } else {
            return "{}";
        }
    }

    /**
     * get Hearing details JSON String from Hearing Request.
     * @return String JSON string of Hearing details
     */
    private String toHearingDetailsJsonString(HearingRequest hearingRequest) {
        if (null != hearingRequest && null != hearingRequest.getHearingDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getHearingDetails());
            return jsonObject.toString();
        } else {
            return "{}";
        }
    }

    /**
     * get Case details JSON String from Hearing Request.
     * @return String JSON string of Case details
     */
    private String toCaseDetailsJsonString(HearingRequest hearingRequest) {
        if (null != hearingRequest && null != hearingRequest.getCaseDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getCaseDetails());
            return jsonObject.toString();
        } else {
            return "{}";
        }
    }

    /**
     * get Party details JSON String from Hearing Request.
     * @return String JSON string of Party details
     */
    private String toPartyDetailsJsonString(HearingRequest hearingRequest) {
        if (null != hearingRequest && null != hearingRequest.getPartyDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getPartyDetails());
            return jsonObject.toString();
        } else {
            return "{}";
        }
    }

}
