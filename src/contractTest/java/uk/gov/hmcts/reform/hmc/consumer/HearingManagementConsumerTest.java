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
import uk.gov.hmcts.reform.hmc.model.Dow;
import uk.gov.hmcts.reform.hmc.model.DowUnavailabilityType;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
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

    private static final PactDslJsonBody pactdsljsonbodyIndividualResponse =
        HearingResponsePactUtil.generateJsonBody(true);
    private static final PactDslJsonBody pactdsljsonbodyOrganisationResponse =
        HearingResponsePactUtil.generateJsonBody(false);

    // Test data 1 - valid HearingRequest
    HearingRequest validHearingRequest = createValidCreateHearingRequest();
    String jsonstringRequest1 = toHearingRequestJsonString(validHearingRequest);

    // Test data 2 - invalid HearingRequest
    HearingRequest invalidHearingRequest = createInvalidCreateHearingRequest();
    String jsonstringRequest2 = toHearingRequestJsonString(invalidHearingRequest);

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    /**
     * create Hearing with Individual - send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact createHearingWithIndividual(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService successfully returns created hearing with individual")
            .uponReceiving("Request to create hearing with individual details")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonstringRequest1, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(pactdsljsonbodyIndividualResponse)
            .toPact();
    }

    /**
     * create Hearing with Organisation- send valid request.
     *
     * @param builder Builder object
     * @return response Response object
     * @throws Exception exception
     */
    @Pact(provider = "hmc_cftHearingService", consumer = "hmc_hearing_service_consumer")
    public RequestResponsePact createHearingWithOrganisation(PactDslWithProvider builder) {
        return builder
            .given("hmc cftHearingService successfully returns created hearing with organisation")
            .uponReceiving("Request to create hearing with organisation details")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonstringRequest1, ContentType.APPLICATION_JSON)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.ACCEPTED.value())
            .body(pactdsljsonbodyOrganisationResponse)
            .toPact();
    }

    /**
     * validation error from create Hearing - send faulty request.
     *
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
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "createHearingWithIndividual")
    public void shouldReturnCreatedHearingWithIndividual(MockServer mockServer) {
        JsonPath response = getRestAssuredJsonPath(mockServer);

        assertThat(response.getString("caseDetails"))
            .isNotEmpty();
        assertThat(response.getString("requestDetails"))
            .isNotEmpty();
        assertThat(response.getString("hearingDetails"))
            .isNotEmpty();
        assertThat(response.getString("partyDetails"))
            .isNotEmpty();
        assertThat(response.getString("status_message"))
            .isEqualTo("Hearing created successfully");
    }

    /**
     * test expects to return the created hearing.
     *
     * @param mockServer MockServer
     * @throws Exception exception
     */
    @Test
    @PactTestFor(pactMethod = "createHearingWithOrganisation")
    public void shouldReturnCreatedHearingWithOrganisation(MockServer mockServer) {
        JsonPath response = getRestAssuredJsonPath(mockServer);

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
     * get RestAssuredJsonPath.
     *
     * @param mockServer MockServer
     */
    public JsonPath getRestAssuredJsonPath(MockServer mockServer) {
        return RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toHearingRequestJsonString(validHearingRequest))
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
     *
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
     *
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
     *
     * @return String JSON string of hearing Request
     */
    private String toHearingRequestJsonString(HearingRequest hearingRequest) {
        JSONObject jsonObject = new JSONObject(hearingRequest);
        String jsonString = jsonObject.toString().replace("autoListFlag", "autolistFlag")
            .replace("caseRestrictedFlag", "caserestrictedFlag")
            .replace("caseSlaStartDate", "caseSLAStartDate");
        logger.debug("hearingRequest to JSON: {}", jsonString);
        return jsonString;
    }

    /**
     * Create Request Details test data.
     *
     * @return requestDetails Request Details
     */
    private RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    /**
     * create HearingDetails test data.
     *
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
     *
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
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    /**
     * Create party details data.
     *
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
        panelPreference1.setRequirementType("MUSTINC");
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
     *
     * @return partyDetails Party Details
     */
    public List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2", createIndividualDetails()));
        return partyDetailsArrayList;
    }

    /**
     * Create party details data.
     *
     * @return partyDetails Party Details
     */
    public List<PartyDetails> partyDetails2() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND2", "DEF2", createIndividualDetails()));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND3", "DEF3", createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P4", "IND4", "DEF4", createIndividualDetails()));
        return partyDetailsArrayList;
    }

    /**
     * create Organisation Details.
     * @return OrganisationDetails organisation Details
     */
    private OrganisationDetails createOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("organisationType");
        organisationDetails.setCftOrganisationID("cftOrganisationId01001");
        return organisationDetails;
    }

    /**
     * create Individual Details.
     * @return IndividualDetails individual Details
     */
    private IndividualDetails createIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Master");
        individualDetails.setFirstName("Harry");
        individualDetails.setLastName("Styles");
        individualDetails.setHearingChannelEmail("harry.styles.neveragin@gmailsss.com");
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel("CBeebies");
        individualDetails.setReasonableAdjustments(createReasonableAdjustments());
        individualDetails.setRelatedParties(createRelatedParties());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        return individualDetails;
    }

    /**
     * create Related Parties.
     * @return List>RelatedParties>
     */
    private List<RelatedParty> createRelatedParties() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("relatedParty1111");
        relatedParty1.setRelationshipType("Family");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("relatedParty3333");
        relatedParty2.setRelationshipType("Blood Brother");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        return relatedParties;
    }

    /**
     * create Party Details.
     * @param partyID party Id
     * @param partyType party Type
     * @param partyRole party Role
     * @param organisationDetails organisation Details
     * @return PartyDetails party details
     */
    private PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                            OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setOrganisationDetails(organisationDetails);
        return partyDetails;
    }

    /**
     * create Party Details.
     * @param partyID party Id
     * @param partyType party Type
     * @param partyRole party Role
     * @param individualDetails individual Details
     * @return PartyDetails
     */
    private PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                            IndividualDetails individualDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);

        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDow(createUnavailableDaysOfTheWeek());
        return partyDetails;
    }

    /**
     * create Unavailability Date Ranges.
     * @return List of String
     */
    private List<String> createReasonableAdjustments() {
        List<String> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add("adjust 1");
        reasonableAdjustments.add("adjust 2");
        reasonableAdjustments.add("adjust 3");
        return reasonableAdjustments;
    }

    /**
     * create Unavailability Date Ranges.
     * @return List of UnavailabilityDow
     */
    private List<UnavailabilityRanges> createUnavailableDateRanges() {
        UnavailabilityRanges unavailabilityRanges1 = new UnavailabilityRanges();
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-01-01"));
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2021-01-15"));
        UnavailabilityRanges unavailabilityRanges2 = new UnavailabilityRanges();
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2021-06-01"));
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2021-06-21"));

        List<UnavailabilityRanges> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }

    /**
     * create Days of the Week Unavailability.
     * @return List of UnavailabilityDow
     */
    private List<UnavailabilityDow> createUnavailableDaysOfTheWeek() {
        UnavailabilityDow unavailabilityDowFriday = new UnavailabilityDow();
        unavailabilityDowFriday.setDowUnavailabilityType(DowUnavailabilityType.PM.getLabel());
        unavailabilityDowFriday.setDow(Dow.FRIDAY.getLabel());
        UnavailabilityDow unavailabilityDowSaturday = new UnavailabilityDow();
        unavailabilityDowSaturday.setDowUnavailabilityType(DowUnavailabilityType.ALL.getLabel());
        unavailabilityDowSaturday.setDow(Dow.SATURDAY.getLabel());
        UnavailabilityDow unavailabilityDowSunday = new UnavailabilityDow();
        unavailabilityDowSunday.setDowUnavailabilityType(DowUnavailabilityType.ALL.getLabel());
        unavailabilityDowSunday.setDow(Dow.SUNDAY.getLabel());

        List<UnavailabilityDow> unavailabilityDows = new ArrayList<>();
        unavailabilityDows.add(unavailabilityDowFriday);
        unavailabilityDows.add(unavailabilityDowSaturday);
        unavailabilityDows.add(unavailabilityDowSunday);
        return unavailabilityDows;
    }

}
