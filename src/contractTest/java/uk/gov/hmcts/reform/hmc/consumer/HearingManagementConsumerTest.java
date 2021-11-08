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
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.utility.HearingResponsePactUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingManagementConsumerTest {

    private static final Logger logger = LoggerFactory.getLogger(HearingManagementConsumerTest.class);

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    private static final String PATH_HEARING = "/hearing";

    // Test data 1 - valid HearingRequest
    HearingRequest validHearingRequest1 = createValidCreateHearingRequest();
    String jsonstringRequest1 = toHearingRequestJsonString(validHearingRequest1);
    HearingResponsePactUtil hrPactUtil = new HearingResponsePactUtil();
    PactDslJsonBody pactdsljsonbodyResponse1 = hrPactUtil.generateJsonBody(validHearingRequest1);

    // Test data 2 - invalid HearingRequest
    HearingRequest invalidHearingRequest2 = createInvalidCreateHearingRequest();
    String jsonstringRequest2 = toHearingRequestJsonString(invalidHearingRequest2);


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
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact createHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("hmc cftHearingService successfully returns created hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonstringRequest1, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(pactdsljsonbodyResponse1)
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact validationErrorFromCreatingHearing(PactDslWithProvider builder) throws Exception {
        return builder
            .given("hmc cftHearingService throws validation error for create hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonstringRequest2, ContentType.APPLICATION_JSON)
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
            .body(toHearingRequestJsonString(validHearingRequest1))
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(202)
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
            .body(toHearingRequestJsonString(invalidHearingRequest2))
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
     * create a Valid Create Hearing Request.
     * @return HearingRequest hearing request
     */
    private HearingRequest createValidCreateHearingRequest() {
        HearingRequest request = new HearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.setPartyDetails(partyDetails());
        request.setRequestDetails(requestDetails());
        return request;
    }

    /**
     * create an Invalid Create Hearing Request.
     * @return HearingRequest hearing request
     */
    private HearingRequest createInvalidCreateHearingRequest() {
        HearingRequest request = new HearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setPartyDetails(partyDetails());
        request.setRequestDetails(requestDetails());
        return request;
    }

    /**
     * get JSON String from hearing Request.
     * @return String JSON string of hearing Request
     */
    private String toHearingRequestJsonString(HearingRequest hearingRequest) {
        JSONObject jsonObject = new JSONObject(hearingRequest);
        logger.debug("hearingRequest to JSON: {}", jsonObject);
        return jsonObject.toString();
    }

    /**
     * Create Request Details test data.
     * @return requestDetails Request Details
     */
    private RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    /**
     * create HearingDetails test data.
     * @return hearingDetails Hearing Details
     */
    public static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.parse("2017-03-01"));
        hearingWindow.setHearingWindowStartDateRange(LocalDate.parse("2017-03-01"));
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setDuration(0);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId("court");
        location1.setLocationType("Location type");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        return hearingDetails;
    }

    /**
     * Create Case Details tets data.
     * @return caseDetails Case Details
     */
    public CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABA1");
        caseDetails.setCaseRef("1111222233334444");
        caseDetails.setRequestTimeStamp(LocalDateTime.parse("2021-08-10T12:20:00"));
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.parse("2017-03-01"));
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    /**
     * Create party details data.
     * @return partyDetails Party Details
     */
    public List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("IND");
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");

        return Lists.newArrayList(partyDetails1, partyDetails2);
    }

}
