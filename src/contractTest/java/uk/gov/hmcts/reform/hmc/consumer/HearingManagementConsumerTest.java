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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_creation_consumer")
    public RequestResponsePact createHearing(PactDslWithProvider builder) throws Exception {
        HearingRequest hearingRequest = createValidCreateHearingRequest();
        System.out.println("VALID REQUEST:" + toHearingRequestJsonString(hearingRequest));
        System.out.println("EXPECTED RESPONSE:" + createCreateHearingResponse(hearingRequest));

        return builder
            .given("hmc_cftHearingService successfully returns created hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(toHearingRequestJsonString(hearingRequest), ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createCreateHearingResponse(hearingRequest))
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_validation_consumer")
    public RequestResponsePact validationErrorFromCreatingHearing(PactDslWithProvider builder) throws Exception {
        HearingRequest hearingRequest = createInvalidCreateHearingRequest();
        System.out.println("INVALID REQUEST:" + toHearingRequestJsonString(hearingRequest));

        return builder
            .given("hmc_cftHearingService throws validation error for create Hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(toHearingRequestJsonString(hearingRequest), ContentType.APPLICATION_JSON)
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
        HearingRequest hearingRequest = createValidCreateHearingRequest();
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toHearingRequestJsonString(hearingRequest))
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
        HearingRequest hearingRequest = createInvalidCreateHearingRequest();
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toHearingRequestJsonString(hearingRequest))
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
     * @param  hearingRequest hearing Request
     * @return PactJsonBody pact JSON body
     */
    private PactDslJsonBody createCreateHearingResponse(HearingRequest hearingRequest) {
        return new PactDslJsonBody()
            .stringType("status_message", "Hearing created successfully")
            .stringType("requestDetails", toRequestDetailsJsonString(hearingRequest))
            .stringType("hearingDetails", toHearingDetailsJsonString(hearingRequest))
            .stringType("caseDetails", toCaseDetailsJsonString(hearingRequest))
            .stringType("partyDetails", toPartyDetailsJsonString(hearingRequest));
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
        System.out.println("hearingRequest to JSON:" + jsonObject.toString());
        return jsonObject.toString();
    }

    /**
     * get Request details JSON String from Hearing Request.
     * @return String JSON string of Request details
     */
    private String toRequestDetailsJsonString(HearingRequest hearingRequest) {
        StringBuilder sb = new StringBuilder();
        if (null != hearingRequest && null != hearingRequest.getRequestDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getRequestDetails());
            sb = sb.append(jsonObject);
        } else {
            sb = sb.append("{}");
        }
        System.out.println("hearingRequest requestDetails to JSON:" + sb);
        return sb.toString();
    }

    /**
     * get Hearing details JSON String from Hearing Request.
     * @return String JSON string of Hearing details
     */
    private String toHearingDetailsJsonString(HearingRequest hearingRequest) {
        StringBuilder sb = new StringBuilder();
        if (null != hearingRequest && null != hearingRequest.getHearingDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getHearingDetails());
            sb = sb.append(jsonObject);
        } else {
            sb = sb.append("{}");
        }
        System.out.println("hearingRequest hearingDetails to JSON:" + sb);
        return sb.toString();
    }

    /**
     * get Case details JSON String from Hearing Request.
     * @return String JSON string of Case details
     */
    private String toCaseDetailsJsonString(HearingRequest hearingRequest) {
        StringBuilder sb = new StringBuilder();
        if (null != hearingRequest && null != hearingRequest.getCaseDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getCaseDetails());
            sb = sb.append(jsonObject);
        } else {
            sb = sb.append("{}");
        }
        System.out.println("hearingRequest caseDetails to JSON:" + sb);
        return sb.toString();
    }

    /**
     * get Party details JSON String from Hearing Request.
     * @return String JSON string of Party details
     */
    private String toPartyDetailsJsonString(HearingRequest hearingRequest) {
        StringBuilder sb = new StringBuilder();
        if (null != hearingRequest && null != hearingRequest.getPartyDetails()) {
            JSONObject jsonObject = new JSONObject(hearingRequest.getPartyDetails());
            sb = sb.append(jsonObject);
        } else {
            sb = sb.append("{}");
        }
        System.out.println("hearingRequest partyDetails to JSON:" + sb);
        return sb.toString();
    }

    public static RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

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

    public List<PartyDetails> partyDetails() {
        PartyDetails partyDetails1 = new PartyDetails();
        partyDetails1.setPartyID("P1");
        partyDetails1.setPartyType("IND");
        partyDetails1.setPartyRole("DEF");

        PartyDetails partyDetails2 = new PartyDetails();
        partyDetails2.setPartyID("P2");
        partyDetails2.setPartyType("IND");
        partyDetails2.setPartyRole("DEF2");

        List<PartyDetails> partyDetails = Lists.newArrayList(partyDetails1, partyDetails2);
        return partyDetails;
    }

}
