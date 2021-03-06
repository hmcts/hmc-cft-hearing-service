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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementGetHearingsConsumerTest extends BasePactTesting {

    private static final String PATH_HEARINGS = "/hearings";
    private static final String VALID_CASE_REF = "9372710950276233";
    private static final String VALID_CASE_STATUS = "LISTED";
    private static final String INVALID_CASE_REF = "9372710950276230";

    private static final String FIELD_STATUS = "status";
    private static final String BAD_REQUEST = "BAD_REQUEST";

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * get Hearings For Valid Case Ref only.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingsForValidCaseRef(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService successfully returns case hearings")
            .uponReceiving("Request to GET hearings for given valid case ref only")
                .path(PATH_HEARINGS + "/" + VALID_CASE_REF)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(HearingResponsePactUtil.generateGetHearingsJsonBody(MSG_200_GET_HEARINGS, VALID_CASE_REF))
            .toPact();
    }

    /**
     * get Hearings For Valid Case Ref and Status.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingsForValidCaseRefAndStatus(PactDslWithProvider builder) {
        return builder
                .given("hmc cftHearingService successfully returns case hearings")
                .uponReceiving("Request to GET hearings for given valid case ref and case status")
                .path(PATH_HEARINGS + "/" + VALID_CASE_REF)
                .method(HttpMethod.GET.toString())
                .query(FIELD_STATUS + "=" + VALID_CASE_STATUS)
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(HearingResponsePactUtil.generateGetHearingsJsonBody(MSG_200_GET_HEARINGS, VALID_CASE_REF,
                        VALID_CASE_STATUS))
                .toPact();
    }

    /**
     * get Hearings For Invalid Case Ref only.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingsForInvalidCaseRef(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService throws validation error while trying to return case hearings")
            .uponReceiving("Request to GET hearings for given invalid case ref")
            .path("/hearings/" + INVALID_CASE_REF)
            .method(HttpMethod.GET.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType("message", MSG_400_GET_HEARINGS)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                      .eachLike("errors", 1)
                      .closeArray())
            .toPact();
    }

    /**
     * get Hearings For Invalid Case Ref and Status.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingsForInvalidCaseRefAndStatus(PactDslWithProvider builder) {
        return builder
                .given("hmc cftHearingService throws validation error while trying to return case hearings")
                .uponReceiving("Request to GET hearings for given invalid case ref and status")
                .path("/hearings/" + INVALID_CASE_REF)
                .method(HttpMethod.GET.toString())
                .query(FIELD_STATUS + "=" + VALID_CASE_STATUS)
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.BAD_REQUEST.value())
                .body(new PactDslJsonBody()
                        .stringType("message", MSG_400_GET_HEARINGS)
                        .stringValue(FIELD_STATUS, BAD_REQUEST)
                        .eachLike("errors", 1)
                        .closeArray())
                .toPact();
    }

    /**
     * test expects to return the found hearings for valid case ref.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForValidCaseRef")
    public void shouldSuccessfullyGetHearingsForCaseRefOnly(MockServer mockServer) {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .when()
            .get(mockServer.getUrl() + PATH_HEARINGS + "/" + VALID_CASE_REF)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .jsonPath();

        assertThat(response.getString("caseHearings")).isNotEmpty();
        assertThat(response.getString("caseRef")).isNotEmpty();
        assertThat(response.getString("hmctsServiceCode")).isNotEmpty();
        assertThat(response.getString("status_message")).isEqualTo(MSG_200_GET_HEARINGS);
    }

    /**
     * test expects to return the found hearings for valid case ref and status.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForValidCaseRefAndStatus")
    public void shouldSuccessfullyGetHearingsForCaseRefAndStatus(MockServer mockServer) {
        JsonPath response = RestAssured
                .given()
                .headers(headers)
                .queryParam(FIELD_STATUS, VALID_CASE_STATUS)
                .when()
                .get(mockServer.getUrl() + PATH_HEARINGS + "/" + VALID_CASE_REF)
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract()
                .body()
                .jsonPath();

        assertThat(response.getString("caseHearings")).isNotEmpty();
        assertThat(response.getString("caseRef")).isNotEmpty();
        assertThat(response.getString("hmctsServiceCode")).isNotEmpty();
        assertThat(response.getString("status_message")).isEqualTo(MSG_200_GET_HEARINGS);
    }

    /**
     * test expects to fail to get hearings for Invalid Case ref.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForInvalidCaseRef")
    public void shouldFailToGetHearings(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .when()
            .get(mockServer.getUrl() + PATH_HEARINGS + "/" + INVALID_CASE_REF)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }

    /**
     * test expects to fail to get hearings.
     *
     * @param mockServer MockServer
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForInvalidCaseRefAndStatus")
    public void shouldFailToGetHearingsForInvalidCaseRefWithStatus(MockServer mockServer) {
        RestAssured
                .given()
                .headers(headers)
                .queryParam(FIELD_STATUS, VALID_CASE_STATUS)
                .when()
                .get(mockServer.getUrl() + PATH_HEARINGS + "/" + INVALID_CASE_REF)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .and()
                .extract()
                .body()
                .jsonPath();
    }

}
