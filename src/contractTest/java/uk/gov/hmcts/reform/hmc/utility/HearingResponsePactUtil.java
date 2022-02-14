package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;

public class HearingResponsePactUtil {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtil.class);

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
            .stringMatcher("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", "A123456789B123456789C123456789")
            .stringMatcher("status", STATUS_OPTIONS_STRING)
            .timestamp("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, Instant.parse("2021-10-29T01:23:34.123456Z"))
            .asBody();

        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body.
     *
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody genericCreateHearingJsonBody(String statusMessage, String hearingRequestID,
                                                               String status, LocalDateTime timeStamp) {
        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
                .stringMatcher("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", hearingRequestID)
                .stringMatcher("status", STATUS_OPTIONS_STRING, status)
                .timestamp("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, timeStamp.atZone(ZoneId.systemDefault()).toInstant())
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
    public static PactDslJsonBody generateDeleteHearingJsonBody(String statusMessage, String hearingRequestId) {
        DeleteHearingRequest deleteHearingRequest = generateDeleteHearingRequest();

        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage, hearingRequestId,
                CANCELLATION_REQUESTED, LocalDateTime.now());

        pactDslJsonBody
            .integerType("versionNumber", deleteHearingRequest.getVersionNumber() + 1)
            .asBody();

        // return constructed body
        logger.info("pactDslJsonBody (DeleteHearing): {}", pactDslJsonBody);
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
            .stringMatcher("hearingID", "^[a-zA-Z0-9]{1,60}$", "ABBBAAA000111NNBA")
            .datetime("hearingRequestDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringType("hearingType")
            .stringMatcher("hmcStatus",STATUS_OPTIONS_STRING)
            .datetime("lastResponseReceivedDateTime", FORMATYYYYMMDDHHMMSSZ,
                      Instant.parse("2021-01-29T02:42:25.123000002Z"))
            .stringType("responseVersion")
            .stringMatcher("hearingListingStatus",STATUS_OPTIONS_STRING)
            .stringMatcher("listAssistCaseStatus", STATUS_OPTIONS_STRING)
            .object("hearingDaySchedule")
              .datetime("hearingStartDateTime", FORMATYYYYMMDDHHMMSSZ)
              .datetime("hearingEndDateTime", FORMATYYYYMMDDHHMMSSZ)
              .stringType("ListAssistSessionID")
              .stringType("hearingVenueId")
              .stringType("hearingRoomId")
              .stringType("hearingJudgeId")
              .object("attendees")
                .stringType("partyID")
                .stringType("hearingSubChannel")
              .closeObject().asBody()
            .closeObject().asBody()
          .closeArray().asBody();
    }

    private static DeleteHearingRequest generateDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCode("REASONCODE25");
        deleteHearingRequest.setVersionNumber(2);
        return  deleteHearingRequest;
    }
}
