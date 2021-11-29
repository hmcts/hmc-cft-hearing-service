package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class HearingResponsePactUtil {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtil.class);

    private static final String FORMATYYYYMMDDHHMMSSSSSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";
    private static final String FORMATYYYYMMDDHHMMSSZ = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private HearingResponsePactUtil() {
        //not called
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateCreateHearingJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .stringType("hearingRequestID")
            .stringType("status")
            .timestamp("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, Instant.parse("2021-10-29T01:23:34.123456Z"))
            .integerType("versionNumber")
            .asBody();

        // return constructed body
        logger.info("pactDslJsonBody: {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateGetHearingsJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);
        // Main Details
        addMainDetails(pactDslJsonBody);

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
     * append main details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private static void addMainDetails(PactDslJsonBody pactDslJsonBody) {
        // append main Details object
        pactDslJsonBody
            .stringMatcher("hmctsServiceCode", "^\\w{4}$", "AB1A")
            .stringMatcher("caseRef", "^\\d{16}$", "9372710950276233");
        addCaseHearings(pactDslJsonBody);
    }

    /**
     * append Case hearing details to given Pact Dsl JSON Body.
     *
     * @param pactDslJsonBody        Pact Dsl JSON Body
     */
    private static void addCaseHearings(PactDslJsonBody pactDslJsonBody) {
        pactDslJsonBody
            .array("caseHearings")
            .object()
            .stringType("hearingID")
            .datetime("hearingRequestDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringType("hearingType")
            .stringMatcher("hmcStatus","HEARING REQUESTED|UPDATE REQUESTED|"
                + "UPDATE SUBMITTED|AWAITING LISTING|LISTED|CANCELLATION REQUESTED|"
                + "EXCEPTION","HEARING REQUESTED")
            .datetime("lastResponseReceivedDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringType("responseVersion")
            .stringMatcher("hearingListingStatus","HEARING REQUESTED|UPDATE REQUESTED|"
                + "UPDATE SUBMITTED|AWAITING LISTING|LISTED|CANCELLATION REQUESTED|"
                + "EXCEPTION","HEARING REQUESTED")
            .stringMatcher("lstAssistCaseStatus",
                    "HEARING REQUESTED|UPDATE REQUESTED|"
                + "UPDATE SUBMITTED|AWAITING LISTING|LISTED|CANCELLATION REQUESTED|"
                + "EXCEPTION","UPDATE REQUESTED")
            .object("hearingDaySchedule")
              .datetime("hearingStartDateTime", FORMATYYYYMMDDHHMMSSZ)
              .datetime("hearingEndDateTime", FORMATYYYYMMDDHHMMSSZ)
              .stringType("ListAssistSessionID")
              .stringType("hearingRoomId")
              .stringType("hearingJudgeId")
            .closeObject().asBody()
          .closeArray().asBody();
    }

}
