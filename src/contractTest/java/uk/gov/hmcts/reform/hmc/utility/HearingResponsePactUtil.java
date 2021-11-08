package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

public class HearingResponsePactUtil {

    static final String FORMATYYYYMMDD = "yyyy-MM-dd";
    static final String FORMATYYYYMMDDHHMMSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";

    /**
     * generate Pact JSON body from given hearing Request
     *
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public PactDslJsonBody generateJsonBody() {
        // Build structural parts of the JSON body
        PactDslJsonBody pdjBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pdjBody, "Hearing created successfully");
        // Request Details object
        addRequestDetails(pdjBody);
        // Hearing Details object
        addHearingDetails(pdjBody);
        // Case Details object
        addCaseDetails(pdjBody);
        // List of Party Details
        addPartyDetails(pdjBody);

        // return constructed body
        return pdjBody;
    }

    /**
     * append status message to given Pact Dsl JSON Body
     *
     * @param pdjBody       Pact Dsl JSON Body
     * @param statusMessage response status message
     */
    private void addStatusMessage(PactDslJsonBody pdjBody, String statusMessage) {
        // append status message
        pdjBody
            .stringType("status_message", statusMessage);
    }

    /**
     * append request details to given Pact Dsl JSON Body
     *
     * @param pdjBody        Pact Dsl JSON Body
     */
    private void addRequestDetails(PactDslJsonBody pdjBody) {
        pdjBody
            .object("requestDetails")
            .datetime("requestTimeStamp", FORMATYYYYMMDDHHMMSSZ)
            .closeObject().asBody();
    }

    /**
     * append hearing details to given Pact Dsl JSON Body
     *
     * @param pdjBody        Pact Dsl JSON Body
     */
    private void addHearingDetails(PactDslJsonBody pdjBody) {
        // append hearingDetails object
        pdjBody
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
            .closeObject().asBody()
            .booleanType("hearingIsLinkedFlag")
            .closeObject().asBody();
    }

    /**
     * append case details to given Pact Dsl JSON Body
     *
     * @param pdjBody     Pact Dsl JSON Body
     */
    private void addCaseDetails(PactDslJsonBody pdjBody) {
        // append requestDetails object
        pdjBody
            .object("caseDetails")
            .stringType("caseRef")
            .stringType("caseDeepLink")
            .stringType("hmctsServiceCode")
            .booleanType("caseRestrictedFlag")
            .array("caseCategories")
            .closeArray().asBody()
            .datetime("requestTimeStamp", "yyyy-MM-dd'T'HH:mm")
            .stringType("caseManagementLocationCode")
            .booleanType("caseAdditionalSecurityFlag")
            .stringType("hmctsInternalCaseName")
            .stringType("publicCaseName")
            .date("caseSlaStartDate", FORMATYYYYMMDD)
            .closeObject().asBody();
    }

    /**
     * append party details to given Pact Dsl JSON Body
     *
     * @param pdjBody          Pact Dsl JSON Body
     */
    private void addPartyDetails(PactDslJsonBody pdjBody) {
        // append requestDetails object
        pdjBody
            .eachLike("partyDetails")
              .stringType("partyID")
              .stringType("partyType")
              .stringType("partyRole")
              .object("individualDetails")
                .stringType("lastName")
                .stringType("preferredHearingChannel")
                .stringType("interpreterLanguage")
                .eachLike("reasonableAdjustments")
                .closeArray().asBody()
                .booleanType("vulnerableFlag")
                .stringType("vulnerabilityDetails")
                .stringType("hearingChannelEmail")
                .stringType("hearingChannelPhone")
                .eachLike("relatedParties")
                  .stringType("relatedPartyID")
                  .stringType("relationshipType")
                .closeArray().asBody()
              .closeObject().asBody()
            .closeArray().asBody();
    }

}
