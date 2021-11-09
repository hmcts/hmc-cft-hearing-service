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
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
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
    PactDslJsonBody pactdsljsonbodyResponse = hrPactUtil.generateJsonBody();

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
    public RequestResponsePact createHearing(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService successfully returns created hearing")
            .uponReceiving("Request to create hearing")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonstringRequest1, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(pactdsljsonbodyResponse)
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     * @param builder builder object
     * @return response RequestResponsePact
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact validationErrorFromCreatingHearing(PactDslWithProvider builder) {
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
    public void shouldReturnCreatedHearing(MockServer mockServer) {
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
    public void shouldReturn400BadRequestForCreateHearing(MockServer mockServer) {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(jsonstringRequest2)
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
        request.setPartyDetails(partyDetails1());
        request.setRequestDetails(requestDetails());
        return request;
    }

    /**
     * create an Invalid Create Hearing Request - omit caseDetails.
     * @return HearingRequest hearing request
     */
    private HearingRequest createInvalidCreateHearingRequest() {
        HearingRequest request = new HearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setPartyDetails(partyDetails2());
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
    public HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingWindow.setHearingWindowStartDateRange(LocalDate.now());
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
        hearingDetails.setPanelRequirements(panelRequirements1());
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
        caseDetails.setRequestTimeStamp(LocalDateTime.now());
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate(LocalDate.now());
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType27");
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
    public PanelRequirements panelRequirements1() {
        List<String> roleType = new ArrayList<>();
        roleType.add("role 1");
        roleType.add("role 2");
        List<String> authorisationTypes = new ArrayList<>();
        authorisationTypes.add("authorisation type 1");
        authorisationTypes.add("authorisation type 2");
        authorisationTypes.add("authorisation type 3");
        List<String> authorisationSubType = new ArrayList<>();
        authorisationSubType.add("authorisation sub 1");
        authorisationSubType.add("authorisation sub 2");
        authorisationSubType.add("authorisation sub 3");
        authorisationSubType.add("authorisation sub 4");
        final PanelPreference panelPreference1 = new PanelPreference();
        panelPreference1.setMemberID("Member 1");
        panelPreference1.setMemberType("Member Type 1");
        panelPreference1.setRequirementType("WHOINC");
        final PanelPreference panelPreference2 = new PanelPreference();
        panelPreference2.setMemberID("Member 2");
        panelPreference2.setMemberType("Member Type 2");
        panelPreference2.setRequirementType("OPTINC");
        final PanelPreference panelPreference3 = new PanelPreference();
        panelPreference3.setMemberID("Member 3");
        panelPreference3.setMemberType("Member Type 3");
        panelPreference3.setRequirementType("EXCLUDE");
        List<PanelPreference> panelPreferences = new ArrayList<>();
        panelPreferences.add(panelPreference1);
        panelPreferences.add(panelPreference2);
        panelPreferences.add(panelPreference3);
        List<String> panelSpecialisms = new ArrayList<>();
        panelSpecialisms.add("Specialism 1");
        panelSpecialisms.add("Specialism 2");
        panelSpecialisms.add("Specialism 3");
        panelSpecialisms.add("Specialism 4");
        panelSpecialisms.add("Specialism 5");

        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(roleType);
        panelRequirements.setAuthorisationTypes(authorisationTypes);
        panelRequirements.setAuthorisationSubType(authorisationSubType);
        panelRequirements.setPanelPreferences(panelPreferences);
        panelRequirements.setPanelSpecialisms(panelSpecialisms);

        return panelRequirements;
    }

    /**
     * Create party details data.
     * @return partyDetails Party Details
     */
    public List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF"));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2"));
        return partyDetailsArrayList;
    }

    /**
     * Create party details data.
     * @return partyDetails Party Details
     */
    public List<PartyDetails> partyDetails2() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF"));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND2", "DEF2"));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND3", "DEF3"));
        partyDetailsArrayList.add(createPartyDetails("P4", "IND4", "DEF4"));
        return partyDetailsArrayList;
    }

    private PartyDetails createPartyDetails(String partyID, String partyType, String partyRole) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        return partyDetails;
    }

}
