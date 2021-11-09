package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;

public class HearingResponsePactUtil {

    static final String FORMATYYYYMMDD = "yyyy-MM-dd";
    static final String FORMATYYYYMMDDHHMMSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";

    /**
     * generate Pact JSON body.
     *
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public PactDslJsonBody generateJsonBody() {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, "Hearing created successfully");
        // Request Details object
        addRequestDetails(pactDslJsonBody);
        // Hearing Details object
        addHearingDetails(pactDslJsonBody);
        // Case Details object
        addCaseDetails(pactDslJsonBody);
        // List of Party Details
        addPartyDetails(pactDslJsonBody);

        // return constructed body
        return pactDslJsonBody;
    }

    /**
     * append status message to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody       Pact Dsl JSON Body
     * @param statusMessage response status message
     */
    private void addStatusMessage(PactDslJsonBody pactDslJsonBody, String statusMessage) {
        // append status message
        pactDslJsonBody
            .stringType("status_message", statusMessage);
    }

    /**
     * append request details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private void addRequestDetails(PactDslJsonBody pactDslJsonBody) {
        pactDslJsonBody
            .object("requestDetails")
            .datetime("requestTimeStamp", FORMATYYYYMMDDHHMMSSZ)
            .closeObject().asBody();
    }

    /**
     * append hearing details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private void addHearingDetails(PactDslJsonBody pactDslJsonBody) {
        // append hearingDetails object
        pactDslJsonBody
            // Simple/default equality checks for other fields so toString will do
            .object("hearingDetails")
            .booleanType("autoListFlag")
            .stringType("hearingType")
            .object("hearingWindow")
              .date("hearingWindowStartDateRange", FORMATYYYYMMDD)
              .date("hearingWindowEndDateRange", FORMATYYYYMMDD)
              .datetime("firstDateTimeMustBe", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .closeObject().asBody()
            .integerType("duration")
            .eachLike("nonStandardHearingDurationReasons")
            .closeArray().asBody()
            .stringType("hearingPriorityType")
            .integerType("numberOfPhysicalAttendees")
            .booleanType("hearingInWelshFlag")
            .eachLike("hearingLocations")
              .stringType("locationType", "Any location type")
              .stringType("locationId", "Any location id")
            .closeArray().asBody()
            .eachLike("facilitiesRequired")
            .closeArray().asBody()
            .stringType("listingComments")
            .stringType("hearingRequester")
            .booleanType("privateHearingRequiredFlag")
            .stringType("leadJudgeContractType")
            .object("panelRequirements")
              .eachLike("roleType")
              .closeArray().asBody()
              .eachLike("authorisationTypes")
              .closeArray().asBody()
              .eachLike("authorisationSubTypes")
              .closeArray().asBody()
              .eachLike("panelPreferences")
                 .stringType("memberID")
                 .stringType("memberType")
                 .eachLike("requirementType", PactDslJsonRootValue.stringMatcher("MUSTINC|OPTINC|EXCLUDE", "MUSTINC"))
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
    private void addCaseDetails(PactDslJsonBody pactDslJsonBody) {
        // append requestDetails object
        pactDslJsonBody
            .object("caseDetails")
            .stringMatcher("hmctsServiceCode", "^\\w{4}$", "A2B4")
            .stringMatcher("caseRef", "^\\d{16}$", "1234567890123456")
            .datetime("requestTimeStamp", "yyyy-MM-dd'T'HH:mm")
            .stringType("externalCaseReference") // max = 70
            .stringType("caseDeepLink") // max = 1024, match URL pattern?
            .stringType("hmctsInternalCaseName") // max = 1024
            .stringType("publicCaseName")// max = 1024
            .booleanType("caseAdditionalSecurityFlag")
            .eachLike("caseCategories")
              .eachLike("categoryType", PactDslJsonRootValue.stringMatcher("caseType|caseSubType", "caseType"))
              .stringType("categoryValue") // max = 70
            .closeArray().asBody()
            .stringType("caseManagementLocationCode")
            .booleanType("caseInterpreterRequiredFlag")
            .booleanType("caserestrictedFlag") // camelCase name?!!**
            .date("caseSLAStartDate", FORMATYYYYMMDD)
            .closeObject().asBody();
    }

    /**
     * append party details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody          Pact Dsl JSON Body
     */
    private void addPartyDetails(PactDslJsonBody pactDslJsonBody) {
        // append requestDetails object
        pactDslJsonBody
            .eachLike("partyDetails")
              .stringType("partyID") // max = 40
              .stringType("partyType") // enum
              .stringType("partyRole") // max = 6
              .object("individualDetails")
                .stringType("title") // max = 40, can't be empty
                .stringType("firstName") // max = 100, can't be empty
                .stringType("lastName") // max = 100, can't be empty
                .stringType("preferredHearingChannel") // max = 70
                .stringType("interpreterLanguage") // max = 10
                .eachLike("reasonableAdjustments")
                    .stringType("reasonableAdjustment") // max = 10
                .closeArray().asBody()
                .booleanType("vulnerableFlag")
                .stringType("vulnerabilityDetails") // max = 256
                .stringType("hearingChannelEmail") // max = 120, invalid email
                .stringMatcher("hearingChannelPhone", "^\\+?(?:[0-9] ?){6,14}[0-9]$", "01234 112233")
                .eachLike("relatedParties")
                  .stringType("relatedPartyID") // max = 2000
                  .stringType("relationshipType") // max = 2000
                .closeArray().asBody()
              .closeObject().asBody()
            .closeArray().asBody();
    }

}
