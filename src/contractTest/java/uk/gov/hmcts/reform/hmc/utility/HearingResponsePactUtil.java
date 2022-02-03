package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class HearingResponsePactUtil {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtil.class);

    private static final String PANEL_MEMBER_ID = "panelMemberId";
    private static final String PARTY_ID = "partyID";
    private static final String HEARING_SUB_CHANNEL = "hearingSubChannel";

    private static final String REGEX_40_CHARS = "^[a-zA-Z0-9]{1,40}$";
    private static final String REGEX_60_CHARS = "^[a-zA-Z0-9]{1,60}$";

    private static final String FORMATYYYYMMDDHHMMSSSSSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";
    private static final String FORMATYYYYMMDDHHMMSSZ = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String STATUS_OPTIONS_STRING = "HEARING_REQUESTED|UPDATE_REQUESTED|"
        + "UPDATE_SUBMITTED|AWAITING_LISTING|LISTED|CANCELLATION_REQUESTED|"
        + "EXCEPTION";

    private HearingResponsePactUtil() {
    }

    /**
     * generate Pact JSON body.
     *
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody genericCreateHearingJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .stringType("hearingRequestID")
            .stringMatcher("status", STATUS_OPTIONS_STRING,"HEARING_REQUESTED")
            .timestamp("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, Instant.parse("2021-10-29T01:23:34.123456Z"))
            .asBody();

        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body.
     *
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateCreateHearingByPostJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage);

        // return constructed body
        logger.info("pactDslJsonBody (CreateHearingByPost): {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateCreateHearingByPutJsonBody(String statusMessage) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage);

        pactDslJsonBody
            .integerType("versionNumber")
            .asBody();

        // return constructed body
        logger.info("pactDslJsonBody (CreateHearingByPut): {}", pactDslJsonBody);
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
            .stringMatcher("hmctsServiceCode", "^[a-zA-Z0-9]{1,4}$", "AB1A")
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
            .stringMatcher("hearingID", REGEX_60_CHARS, "ABBBAAA000111NNBA")
            .datetime("hearingRequestDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringMatcher("hearingType", REGEX_60_CHARS, "RRRRAASSSS")
            .stringMatcher("hmcStatus",STATUS_OPTIONS_STRING)
            .datetime("lastResponseReceivedDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringMatcher("responseVersion", REGEX_60_CHARS, "27")
            .stringMatcher("hearingListingStatus",STATUS_OPTIONS_STRING)
            .stringMatcher("listAssistCaseStatus", STATUS_OPTIONS_STRING)
            .array("hearingDaySchedule")
              .object()
                .datetime("hearingStartDateTime", FORMATYYYYMMDDHHMMSSZ)
                .datetime("hearingEndDateTime", FORMATYYYYMMDDHHMMSSZ)
                .stringType("listAssistSessionID")
                .stringType("hearingVenueId")
                .stringType("hearingRoomId")
                .stringMatcher("hearingJudgeId", REGEX_60_CHARS, "Judge23")
                .array("panelMemberIds")
                  .object()
                  .stringMatcher(PANEL_MEMBER_ID, REGEX_60_CHARS, "Panelmbr11")
                  .closeObject()
                .object()
                .stringMatcher(PANEL_MEMBER_ID, REGEX_60_CHARS, "Panelmbr12")
                .closeObject()
                .object()
                .stringMatcher(PANEL_MEMBER_ID, REGEX_60_CHARS, "Panelmbr13")
                .closeObject()
                .object()
                .stringMatcher(PANEL_MEMBER_ID, REGEX_60_CHARS, "Panelmbr14")
                .closeObject()
                .closeArray()
                .array("attendees")
                  .object()
                  .stringMatcher(PARTY_ID,REGEX_40_CHARS, "party1236")
                  .stringMatcher(HEARING_SUB_CHANNEL, REGEX_60_CHARS, "subchannel76")
                  .closeObject()
                .object()
                .stringMatcher(PARTY_ID,REGEX_40_CHARS, "party1237")
                .stringMatcher(HEARING_SUB_CHANNEL, REGEX_60_CHARS, "subchannel77")
                .closeObject()
                .object()
                .stringMatcher(PARTY_ID,REGEX_40_CHARS, "party1238")
                .stringMatcher(HEARING_SUB_CHANNEL, REGEX_60_CHARS, "subchannel78")
                .closeObject()
                .closeArray()
              .closeObject()
            .closeArray()
          .closeObject()
        .closeArray().asBody();
    }

}
