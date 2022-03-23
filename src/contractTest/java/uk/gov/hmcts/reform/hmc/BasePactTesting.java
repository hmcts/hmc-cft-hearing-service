package uk.gov.hmcts.reform.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BasePactTesting {

    protected static final Logger logger = LoggerFactory.getLogger(BasePactTesting.class);

    public static final String PROVIDER_NAME = "hmcHearingServiceProvider";
    public static final String CONSUMER_NAME = "hmcHearingServiceConsumer";

    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    protected static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    protected static final String VALID_CASE_REF = "9372710950276233";
    protected static final String VALID_CASE_STATUS = "UPDATED";

    public static final String MSG_200_GET_HEARINGS = "Success (with content)";
    public static final String MSG_400_GET_HEARINGS = "Invalid request";
    public static final String MSG_200_DELETE_HEARING = "Success (with content)";
    public static final String MSG_400_DELETE_HEARING = "Invalid request";


    protected static final Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    private final Random random = new Random();

    /**
     * generate Hearing Request.
     *
     * @return HearingRequest hearing request
     */
    protected CreateHearingRequest generateHearingRequest(String caseRef) {
        CreateHearingRequest request = new CreateHearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails(caseRef));
        request.setPartyDetails(partyDetails1());
        request.setRequestDetails(requestDetails());
        return request;
    }

    /**
     * generate Invalid Hearing Request - omit caseDetails.
     *
     * @return HearingRequest hearing request
     */
    protected CreateHearingRequest generateInvalidHearingRequest() {
        CreateHearingRequest request = new CreateHearingRequest();
        request.setHearingDetails(hearingDetails());
        request.setPartyDetails(partyDetails2());
        request.setRequestDetails(requestDetails());
        return request;
    }

    /**
     * generate Delete Hearing Request.
     *
     * @return DeleteHearingRequest delete hearing request
     */
    protected DeleteHearingRequest generateDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCode("1XXX1");
        return deleteHearingRequest;
    }

    /**
     * generate invalid Delete Hearing Request.
     *
     * @return DeleteHearingRequest delete hearing request
     */
    protected DeleteHearingRequest generateInvalidDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        return deleteHearingRequest;
    }

    /**
     * get JSON String from hearing Request.
     *
     * @return String JSON string of hearing Request
     */
    protected String jsonCreatedHearingResponse(CreateHearingRequest hearingRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(hearingRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.debug("hearingRequest to JSON: {}", jsonString);
        return jsonString;
    }

    /**
     * get JSON String from hearing Request.
     *
     * @return String JSON string of hearing Request
     */
    protected String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.info("toJsonString: {}", jsonString);
        return jsonString;
    }

    /**
     * Create Request Details test data.
     *
     * @return requestDetails Request Details
     */
    protected RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(LocalDateTime.now());
        return requestDetails;
    }

    /**
     * create HearingDetails test data.
     *
     * @return hearingDetails Hearing Details
     */
    protected HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        hearingDetails.setHearingWindow(hearingWindow());
        hearingDetails.setDuration(1);
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
     * is random number ODD?.
     *
     * @return boolean true if odd
     */
    private boolean isRandomlyOdd() {
        int x = random.nextInt();
        if (x < 0) {
            x *= -1;
        }
        x = x % 2;
        return (x > 0);
    }

    /**
     * Create Hearing Window test data.
     *
     * @return hearingWindow hearing Window
     */
    protected HearingWindow hearingWindow() {
        HearingWindow hearingWindow = new HearingWindow();
        if (isRandomlyOdd()) {
            logger.info("using hearing window first date");
            hearingWindow.setFirstDateTimeMustBe(LocalDateTime.now());
        } else {
            logger.info("using hearing window date range");
            hearingWindow.setHearingWindowStartDateRange(LocalDate.now());
            hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        }
        return hearingWindow;
    }

    /**
     * Create Case Details test data.
     *
     * @return caseDetails Case Details
     */
    protected CaseDetails caseDetails(String caseRef) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABBA1");
        caseDetails.setCaseRef(caseRef);
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
    protected PanelRequirements panelRequirements1() {
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
    protected List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND", "DEF3", createIndividualDetails(),
                                                     createOrganisationDetails()));
        return partyDetailsArrayList;
    }

    /**
     * Create party details data.
     *
     * @return partyDetails Party Details
     */
    private List<PartyDetails> partyDetails2() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND2", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND3", "DEF3", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P4", "IND4", "DEF4", createIndividualDetails(),
                                                     createOrganisationDetails()));
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
                                            IndividualDetails individualDetails,
                                            OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        if (null != individualDetails) {
            partyDetails.setIndividualDetails(individualDetails);
        }
        if (null != organisationDetails) {
            partyDetails.setOrganisationDetails(organisationDetails);
        }
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDow(createUnavailabilityDows());
        return partyDetails;
    }

    /**
     * create Reasonable Adjustments.
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
     * create Unavailability Dow.
     * @return List of String
     */
    private List<UnavailabilityDow> createUnavailabilityDows() {
        List<UnavailabilityDow> unavailabilityDows = new ArrayList<>();
        UnavailabilityDow unavailabilityDow1 = new UnavailabilityDow();
        unavailabilityDow1.setDow("DOW1");
        unavailabilityDow1.setDowUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow1);
        UnavailabilityDow unavailabilityDow2 = new UnavailabilityDow();
        unavailabilityDow2.setDow("DOW1");
        unavailabilityDow2.setDowUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow2);
        return unavailabilityDows;
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

}
