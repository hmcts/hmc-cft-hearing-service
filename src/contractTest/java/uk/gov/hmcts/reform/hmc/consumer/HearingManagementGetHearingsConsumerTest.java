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
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementGetHearingsConsumerTest extends BasePactTesting {

    private static final String PATH_HEARINGS = "/hearings";
    private static final String VALID_CASE_REF = "9372710950276233";
    private static final String VALID_CASE_STATUS = "UPDATED";
    private static final String INVALID_CASE_REF = "9372710950276230";


    // Test data 1 - valid HearingRequest
    HearingRequest validHearingRequest = generateHearingRequest(VALID_CASE_REF);

    private static final String EXPECTED_STATUS_MESSAGE = "Hearing obtained successfully";

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * get Hearings For Valid Case Ref.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact getHearingsForValidCaseRef(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService successfully returns case hearings")
            .uponReceiving("Request to get hearings for given valid case ref and (optionally) case status")
                .path(PATH_HEARINGS + "/" + VALID_CASE_REF)
                .method(HttpMethod.GET.toString())
                .query("status=UPDATED")
                .headers(headers)
            .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(HearingResponsePactUtil.generateGetHearingsJsonBody(EXPECTED_STATUS_MESSAGE))
            .toPact();
    }

    /**
     * get Hearings For Invalid Case Ref.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact getHearingsForInvalidCaseRef(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService throws validation error while trying to return case hearings")
            .uponReceiving("Request to get hearings for given invalid case ref")
            .path("/hearings/" + INVALID_CASE_REF)
            .method(HttpMethod.GET.toString())
            .query("status=UPDATED")
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType("message", "Case ref details is invalid")
                      .stringValue("status", "BAD_REQUEST")
                      .eachLike("errors", 1)
                      .closeArray())
            .toPact();
    }

    /**
     * test expects to return the found hearings.
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForValidCaseRef")
    public void shouldSuccessfullyGetHearings(MockServer mockServer) {
        JsonPath response = getRestAssuredJsonPath(mockServer, VALID_CASE_REF,
                               VALID_CASE_STATUS, 200);

        assertThat(response.getString("caseHearings"))
            .isNotEmpty();
        assertThat(response.getString("status_message"))
            .isEqualTo(EXPECTED_STATUS_MESSAGE);
    }

    /**
     * test expects to fail to get hearings.
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "getHearingsForInvalidCaseRef")
    public void shouldFailToGetHearings(MockServer mockServer) {
        getRestAssuredJsonPath(mockServer, INVALID_CASE_REF,
                               VALID_CASE_STATUS, 400);
    }

    public JsonPath getRestAssuredJsonPath(MockServer mockServer,
                                           String caseRef,
                                           String caseStatus,
                                           int expectedStatus) {
        return RestAssured
            .given()
            .headers(headers)
            .queryParam("status", caseStatus)
            .when()
            .get(mockServer.getUrl() + PATH_HEARINGS + "/" + caseRef)
            .then()
            .statusCode(expectedStatus)
            .and()
            .extract()
            .body()
            .jsonPath();

    }

}
