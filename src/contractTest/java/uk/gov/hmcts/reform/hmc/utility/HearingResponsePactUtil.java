package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import uk.gov.hmcts.reform.hmc.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class HearingResponsePactUtil {
    DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
    //default time zone
    ZoneId defaultZoneId = ZoneId.systemDefault();
    static final String FORMATYYYYMMDD = "yyyy-MM-dd";

    /**
     * generate Pact JSON body from given hearing Request
     * @param hearingRequest
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public PactDslJsonBody generateJsonBody(HearingRequest hearingRequest) {
        // Build structural parts of the JSON body
        PactDslJsonBody pdjBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pdjBody,"Hearing created successfully");
        // Request Details object
        addRequestDetails(pdjBody,hearingRequest.getRequestDetails());
        // Hearing Details object
        addHearingDetails(pdjBody,hearingRequest.getHearingDetails());
        // Case Details object
        addCaseDetails(pdjBody,hearingRequest.getCaseDetails());
        // List of Party Details
        addPartyDetails(pdjBody,hearingRequest.getPartyDetails());

        // return constructed body
        return pdjBody;
    }

    /**
     * append status message to given Pact Dsl JSON Body
     * @param pdjBody Pact Dsl JSON Body
     * @param statusMessage response status message
     */
    private void addStatusMessage(PactDslJsonBody pdjBody, String statusMessage)  {
        // append status message
        pdjBody
            .stringType("status_message", statusMessage);
    }

    /**
     * append request details to given Pact Dsl JSON Body
     * @param pdjBody Pact Dsl JSON Body
     * @param requestDetails request details from hearing Request
     */
     private void addRequestDetails(PactDslJsonBody pdjBody, RequestDetails requestDetails)  {
         pdjBody
             .object("requestDetails")
             .datetime("requestTimeStamp", "yyyy-MM-dd'T'HH:mm:SSSSSS",
                      Instant.parse(requestDetails.getRequestTimeStamp().format(formatterDateTime)))
             .closeObject().asBody();
     }

    /**
     * append hearing details to given Pact Dsl JSON Body
     * @param pdjBody Pact Dsl JSON Body
     * @param hearingDetails hearing details from hearing Request
     */
     private void addHearingDetails(PactDslJsonBody pdjBody, HearingDetails hearingDetails)  {
        // append hearingDetails object
        pdjBody
            // Simple/default equality checks for other fields so toString will do
            .object("hearingDetails")
            .booleanType("autoListFlag", hearingDetails.getAutoListFlag())
            .stringType("hearingType", hearingDetails.getHearingType())
            .object("hearingWindow")
            .date("hearingWindowStartDateRange", FORMATYYYYMMDD,
                  Date.from(hearingDetails.getHearingWindow().getHearingWindowStartDateRange().atStartOfDay(defaultZoneId).toInstant()))
            .date("hearingWindowEndDateRange", FORMATYYYYMMDD,
                  Date.from(hearingDetails.getHearingWindow().getHearingWindowEndDateRange().atStartOfDay(defaultZoneId).toInstant()))
            .datetime("firstDateTimeMustBe", "yyyy-MM-dd'T'HH:mm:ss'Z'",
                       null != hearingDetails.getHearingWindow().getFirstDateTimeMustBe() ?
                           Instant.parse(hearingDetails.getHearingWindow().getFirstDateTimeMustBe().format(DateTimeFormatter.ISO_INSTANT))
                            : Instant.parse(LocalDateTime.now().format(formatterDateTime)))
            .closeObject().asBody()
            .integerType("duration", hearingDetails.getDuration())
            .eachLike("nonStandardHearingDurationReasons")
            .closeArray().asBody()
            .stringType("hearingPriorityType", hearingDetails.getHearingPriorityType())
            .integerType(
                "numberOfPhysicalAttendees",
                hearingDetails.getNumberOfPhysicalAttendees()
            )
            .booleanType("hearingInWelshFlag", hearingDetails.getHearingInWelshFlag())
            .eachLike("hearingLocations")
            .stringType("locationType", "Any location type")
            .stringType("locationId", "Any location id")
            .closeArray().asBody()
            .eachLike("facilitiesRequired")
            .closeArray().asBody()
            .stringType("listingComments", hearingDetails.getListingComments())
            .stringType("hearingRequester", hearingDetails.getHearingRequester())
            .booleanType(
                "privateHearingRequiredFlag",
                hearingDetails.getPrivateHearingRequiredFlag()
            )
            .stringType(
                "leadJudgeContractType",
                hearingDetails.getLeadJudgeContractType()
            )
            .object("panelRequirements")
            .closeObject().asBody()
            .booleanType("hearingIsLinkedFlag", hearingDetails.getHearingIsLinkedFlag())
            .closeObject().asBody();
    }

    /**
     * append case details to given Pact Dsl JSON Body
     * @param pdjBody Pact Dsl JSON Body
     * @param caseDetails case details from hearing Request
     */
    private void addCaseDetails(PactDslJsonBody pdjBody, CaseDetails caseDetails)  {
        // append requestDetails object
        pdjBody
            .object("caseDetails")
            .stringType("caseRef", caseDetails.getCaseRef())
            .stringType("caseDeepLink", caseDetails.getCaseDeepLink())
            .stringType("hmctsServiceCode", caseDetails.getHmctsServiceCode())
            .booleanType(
                "caseRestrictedFlag",
                caseDetails.getCaseRestrictedFlag()
            )
            .array("caseCategories")
            .closeArray().asBody()
            .datetime("requestTimeStamp", "yyyy-MM-dd'T'HH:mm",
                      null != caseDetails.getRequestTimeStamp() ?
                          Instant.parse(caseDetails.getRequestTimeStamp().format(formatterDateTime))
                          : null)
            .stringType(
                "caseManagementLocationCode",
                caseDetails.getCaseManagementLocationCode()
            )
            .booleanType(
                "caseAdditionalSecurityFlag",
                caseDetails.getCaseAdditionalSecurityFlag()
            )
            .stringType(
                "hmctsInternalCaseName",
                caseDetails.getHmctsInternalCaseName()
            )
            .stringType("publicCaseName", caseDetails.getPublicCaseName())
            .date("caseSlaStartDate", FORMATYYYYMMDD,
                  Date.from(caseDetails.getCaseSlaStartDate().atStartOfDay(defaultZoneId).toInstant()))
            .closeObject().asBody();
    }

    /**
     * append party details to given Pact Dsl JSON Body
     * @param pdjBody Pact Dsl JSON Body
     * @param partyDetailsList case details from hearing Request
     */
    private void addPartyDetails(PactDslJsonBody pdjBody, List<PartyDetails> partyDetailsList)  {
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
