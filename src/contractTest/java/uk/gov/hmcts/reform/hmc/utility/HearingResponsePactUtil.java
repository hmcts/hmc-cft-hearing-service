package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class HearingResponsePactUtil {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtil.class);

    private static final String FORMATYYYYMMDD = "yyyy-MM-dd";
    private static final String FORMATYYYYMMDDHHMMSSSSSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";
    private static final String FORMATYYYYMMDDHHMMSSZ = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private HearingResponsePactUtil() {
        //not called
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);
        // Request Details object
        addRequestDetails(pactDslJsonBody);
        // Hearing Details object
        addHearingDetails(pactDslJsonBody);
        // Case Details object
        addCaseDetails(pactDslJsonBody);
        // List of Party Details
        addPartyDetails(pactDslJsonBody);

        // return constructed body
        logger.info("pactDslJsonBody: {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    /**
     * append status message to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody       Pact Dsl JSON Body
     * @param statusMessage response status message
     */
    private static void addStatusMessage(PactDslJsonBody pactDslJsonBody, String statusMessage) {
        // append status message
        pactDslJsonBody
            .stringType("status_message", statusMessage);
    }

    /**
     * append request details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private static void addRequestDetails(PactDslJsonBody pactDslJsonBody) {
        pactDslJsonBody
            .object("requestDetails")
            .datetime("requestTimeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, Instant.parse("2021-10-29T01:23:34.123456Z"))
            .closeObject().asBody();
    }

    /**
     * append hearing details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private static void addHearingDetails(PactDslJsonBody pactDslJsonBody) {
        // append hearingDetails object
        pactDslJsonBody
            // Simple/default equality checks for other fields so toString will do
            .object("hearingDetails")
            .booleanType("autoListFlag")
            .stringType("hearingType")
            .object("hearingWindow")
                .date("hearingWindowStartDateRange", FORMATYYYYMMDD)
                .date("hearingWindowEndDateRange", FORMATYYYYMMDD)
                .datetime("firstDateTimeMustBe", FORMATYYYYMMDDHHMMSSZ, Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .closeObject().asBody()
            .integerType("duration", 1)
            .array("nonStandardHearingDurationReasons")
            .closeArray().asBody()
            .stringType("hearingPriorityType")
            .integerType("numberOfPhysicalAttendees")
            .booleanType("hearingInWelshFlag")
            .array("hearingLocations")
                    .stringType("locationType")
                    .stringType("locationId")
            .closeArray()
            .array("facilitiesRequired")
                .stringType("facilityRequired") //??
            .closeArray().asBody()
            .stringType("listingComments", "One big listing of comments")
            .stringType("hearingRequester", "hearing requester")
            .booleanType("privateHearingRequiredFlag", false)
            .stringType("leadJudgeContractType", "Lead contract type 1")
            .object("panelRequirements")
              .array("roleType")
              .closeArray().asBody()
              .array("authorisationTypes")
              .closeArray().asBody()
              .array("authorisationSubTypes")
              .closeArray().asBody()
              .array("panelPreferences")
                .object()
                 .stringType("memberID", "1122334444")
                 .stringType("memberType", "Member Type 1")
                 .stringType("requirementType", "MUSTINC")
                .closeObject()
              .closeArray().asBody()
              .eachLike("panelSpecialisms")
              .closeArray().asBody()
            .closeObject().asBody()
            .booleanType("hearingIsLinkedFlag")
            .closeObject().asBody();
    }

    /**
     * append case details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody     Pact Dsl JSON Body
     */
    private static void addCaseDetails(PactDslJsonBody pactDslJsonBody) {
        // append requestDetails object
        pactDslJsonBody
            .object("caseDetails")
            .stringMatcher("hmctsServiceCode", "^\\w{4}$", "A2B4")
            .stringMatcher("caseRef", "^\\d{16}$", "1234567890123456")
            .datetime("requestTimeStamp", "yyyy-MM-dd'T'HH:mm", Instant.parse("2021-11-30T02:42:25.123000002Z"))
            .stringType("externalCaseReference", "237dkjdhkihji933333333111dddffff2434") // max = 70
            .stringType("caseDeepLink", "http://localhost/link") // max = 1024, match URL pattern?
            .stringType("hmctsInternalCaseName", "HHGASlLLGGGGGGHJKKKKLLLLLKJ") // max = 1024
            .stringType("publicCaseName","DEFRAUDED ETC")// max = 1024
            .booleanType("caseAdditionalSecurityFlag", true)
            .eachLike("caseCategories")
              .eachLike("categoryType", PactDslJsonRootValue.stringMatcher("caseType|caseSubType", "caseType"))
              .stringType("categoryValue", "CATEGORY VALUE 1") // max = 70
            .closeArray().asBody()
            .stringType("caseManagementLocationCode", "LOCATION 1")
            .booleanType("caseInterpreterRequiredFlag", false)
            .booleanType("caserestrictedFlag", false) // camelCase name?!!**
            .date("caseSLAStartDate", FORMATYYYYMMDD)
            .closeObject().asBody();
    }

    /**
     * append party details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody          Pact Dsl JSON Body
     */
    private static void addPartyDetails(PactDslJsonBody pactDslJsonBody) {
        // append requestDetails object
        pactDslJsonBody
            .array("partyDetails")
                .object()
                    .stringType("partyID") // max = 40
                    .stringType("partyType")
                    .stringType("partyRole") // max = 6
                    .object("individualDetails")
                        .stringType("title", "HIS ROYAL HIGHNESS") // max = 40, can't be empty
                        .stringType("firstName", "CHARLES") // max = 100, can't be empty
                        .stringType("lastName", "WINDSOR") // max = 100, can't be empty
                        .stringType("preferredHearingChannel", "CHANNEL 1") // max = 70
                        .stringType("interpreterLanguage", "FARSI") // max = 10
                        .array("reasonableAdjustments")
                            .stringType("reasonableAdjustment")
                        .closeArray().asBody()
                        .booleanType("vulnerableFlag", true)
                        .stringType("vulnerabilityDetails") // max = 256
                        .stringType("hearingChannelEmail") // max = 120, invalid email
                        .stringMatcher("hearingChannelPhone", "^\\+?(?:[0-9] ?){6,14}[0-9]$", "01234 112233")
                        .eachLike("relatedParties")
                            .stringType("relatedPartyID", "RELATED PARTY 1") // max = 2000
                            .stringType("relationshipType", "RELATIONSHIP TYPE 1") // max = 2000
                        .closeArray().asBody()
                    .closeObject()
                    .object("organisationDetails")
                        .stringType("name", "CGI LIMITED") // max = 2000, can't be empty
                        .stringType("organisationType", "BUSINESS") // max = 60, can't be empty
                        .stringType("cftOrganisationID", "CGI122333111X") // max = 60, can't be empty
                    .closeObject()
                .closeObject()
            .closeArray().asBody();
    }

}
