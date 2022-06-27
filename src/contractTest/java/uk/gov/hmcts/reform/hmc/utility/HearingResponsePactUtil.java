package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;

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
            .stringMatcher("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", "A123456789B123456789C123456789")
            .stringMatcher("status", STATUS_OPTIONS_STRING, "HEARING_REQUESTED")
            .datetime("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, Instant.parse("2021-10-29T01:23:34.123456Z"))
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
                .datetime("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, timeStamp.atZone(ZoneId.systemDefault()).toInstant())
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
        logger.debug("pactDslJsonBody (CreateHearingByPost): {}", pactDslJsonBody);
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
            .integerType("versionNumber", 1)
            .asBody();

        // return constructed body
        logger.debug("pactDslJsonBody (CreateHearingByPut): {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateGetHearingsJsonBody(String statusMessage, String caseRef) {
        GetHearingsResponse getHearingsResponse = generateGetHearingsResponse(caseRef);

        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);
        // Main Details
        addMainDetails(pactDslJsonBody, caseRef, getHearingsResponse);

        // return constructed body
        logger.debug("pactDslJsonBody: {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    /**
     * generate Pact JSON body for Get Hearings response.
     *
     * @param statusMessage status message
     * @param  caseRef  case Ref
     * @param  caseStatus status
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generateGetHearingsJsonBody(String statusMessage, String caseRef, String caseStatus) {
        GetHearingsResponse getHearingsResponse = generateGetHearingsResponse(caseRef, caseStatus);

        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        // Starting with the status message
        addStatusMessage(pactDslJsonBody, statusMessage);
        // Main Details
        addMainDetails(pactDslJsonBody, caseRef, getHearingsResponse);

        // return constructed body
        logger.debug("pactDslJsonBody: {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    public static PactDslJsonBody generateDeleteHearingJsonBody(String statusMessage, String hearingRequestId) {
        DeleteHearingRequest deleteHearingRequest = generateDeleteHearingRequest();

        // Build structural parts of the JSON body
        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage, hearingRequestId,
                CANCELLATION_REQUESTED, LocalDateTime.now());

        // return constructed body
        logger.info("pactDslJsonBody (DeleteHearing): {}", pactDslJsonBody);
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
     * @param pactDslJsonBody Pact Dsl JSON Body
     * @param  caseRef case ref
     * @param  response GetHearingsResponse object
     */
    private static void addMainDetails(PactDslJsonBody pactDslJsonBody, String caseRef,
                                       GetHearingsResponse response) {
        // append main Details object
        pactDslJsonBody
                .stringMatcher("hmctsServiceCode", "^[a-zA-Z0-9]{1,4}$", response.getHmctsServiceCode())
                .stringMatcher("caseRef", "^\\d{16}$", caseRef)
                .object("caseHearings", addCaseHearings(response.getCaseHearings()));
    }

    /**
     * build PactDslJsonArray for given case hearings.
     *
     * @param caseHearings List
     */
    private static PactDslJsonArray addCaseHearings(List<CaseHearing> caseHearings) {
        PactDslJsonArray pactDslJsonArray = new PactDslJsonArray();

        pactDslJsonArray.array();
        caseHearings.stream().forEach(caseHearing ->
            pactDslJsonArray
                    .object()
                    .stringMatcher("hearingID", REGEX_60_CHARS, caseHearing.getHearingId().toString())
                    .datetime("hearingRequestDateTime", FORMATYYYYMMDDHHMMSSZ,
                            Instant.from(caseHearing.getHearingRequestDateTime().atZone(ZoneOffset.UTC)))
                    .stringMatcher("hearingType", REGEX_60_CHARS, caseHearing.getHearingType())
                    .stringMatcher("hmcStatus",STATUS_OPTIONS_STRING, caseHearing.getHmcStatus())
                    .datetime("lastResponseReceivedDateTime", FORMATYYYYMMDDHHMMSSZ,
                            Instant.from(caseHearing.getLastResponseReceivedDateTime().atZone(ZoneOffset.UTC)))
                    .integerType("requestVersion", caseHearing.getRequestVersion())
                    .stringMatcher("hearingListingStatus",STATUS_OPTIONS_STRING,
                            caseHearing.getHearingListingStatus())
                    .stringMatcher("listAssistCaseStatus", STATUS_OPTIONS_STRING,
                            caseHearing.getListAssistCaseStatus())
                    .object("hearingDaySchedule", addHearingDaySchedules())
                    .closeObject()
        );
        pactDslJsonArray.close();
        logger.debug("pactDslJsonArray: {}", pactDslJsonArray);
        return pactDslJsonArray;
    }

    /**
     * build PactDslJsonArray of hearing day schedules.
     */
    public static PactDslJsonArray addHearingDaySchedules() {
        PactDslJsonArray pactDslJsonArray = new PactDslJsonArray();

        pactDslJsonArray.array();
        generateHearingDaySchedules().stream().forEach(hearingDaySchedule ->
            pactDslJsonArray
                    .object()
                    .datetime("hearingStartDateTime", FORMATYYYYMMDDHHMMSSZ,
                            Instant.from(hearingDaySchedule.getHearingStartDateTime().atZone(ZoneOffset.UTC)))
                    .datetime("hearingEndDateTime", FORMATYYYYMMDDHHMMSSZ,
                            Instant.from(hearingDaySchedule.getHearingEndDateTime().atZone(ZoneOffset.UTC)))
                    .stringType("listAssistSessionID", hearingDaySchedule.getListAssistSessionId())
                    .stringType("hearingVenueId", hearingDaySchedule.getHearingVenueId())
                    .stringType("hearingRoomId", hearingDaySchedule.getHearingRoomId())
                    .stringMatcher("hearingJudgeId", REGEX_60_CHARS, hearingDaySchedule.getHearingJudgeId())
                    .object("panelMemberIds", addPanelMemberIds())
                    .object("attendees", addAttendees())
                    .closeObject()
        );
        pactDslJsonArray.close();
        logger.debug("pactDslJsonArray: {}", pactDslJsonArray);
        return pactDslJsonArray;
    }

    /**
     * create Array from list of string Panel Member Ids.
     *
     * @return pactDslJsonArray PactDslJsonArray
     */
    public static PactDslJsonArray addPanelMemberIds() {
        return addArrayListStrings(PANEL_MEMBER_ID, REGEX_60_CHARS, generatePanelMemberIds());
    }

    /**
     * generic create Array from list of strings.
     * @param fieldName field name
     * @param regex regular expression
     * @param values list of values
     * @return pactDslJsonArray PactDslJsonArray
     */
    public static PactDslJsonArray addArrayListStrings(String fieldName, String regex, List<String> values) {
        PactDslJsonArray pactDslJsonArray = new PactDslJsonArray();
        pactDslJsonArray.array();
        values.stream().forEach(value ->
            pactDslJsonArray
                    .object()
                    .stringMatcher(fieldName, regex, value)
                    .closeObject()
        );
        pactDslJsonArray.closeArray();
        if (logger.isDebugEnabled()) {
            logger.debug(pactDslJsonArray.toString());
        }
        return pactDslJsonArray;
    }

    /**
     * build Array of attendees.
     *
     * @return pactDslJsonArray PactDslJsonArray
     */
    public static PactDslJsonArray addAttendees() {
        PactDslJsonArray pactDslJsonArray = new PactDslJsonArray();
        pactDslJsonArray.array();
        generateAttendees().stream().forEach(attendee ->
            pactDslJsonArray
                  .object()
                  .stringMatcher(PARTY_ID,REGEX_40_CHARS, attendee.getPartyId())
                  .stringMatcher(HEARING_SUB_CHANNEL, REGEX_60_CHARS, attendee.getHearingSubChannel())
                  .closeObject()
        );
        pactDslJsonArray.closeArray();
        if (logger.isDebugEnabled()) {
            logger.debug(pactDslJsonArray.toString());
        }
        return pactDslJsonArray;
    }

    /**
     * generate GetHearingsResponse for given caseRef.
     *
     * @param  caseRef case ref
     * @return GetHearingsResponse response
     */
    private static GetHearingsResponse generateGetHearingsResponse(String caseRef) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setCaseHearings(generateCaseHearings());
        getHearingsResponse.setHmctsServiceCode("svc1");
        return getHearingsResponse;
    }

    /**
     * generate GetHearingsResponse for given caseRef and case Status.
     *
     * @param caseRef case ref
     * @param caseStatus case status
     * @return GetHearingsResponse response
     */
    private static GetHearingsResponse generateGetHearingsResponse(String caseRef, String caseStatus) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        getHearingsResponse.setCaseHearings(generateCaseHearings(caseStatus));
        getHearingsResponse.setHmctsServiceCode("svc1");
        return getHearingsResponse;
    }

    /**
     * generate a list of case hearing.
     *
     * @return caseHearings List
     */
    private static List<CaseHearing> generateCaseHearings() {
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(generateCaseHearing(1));
        caseHearings.add(generateCaseHearing(2));
        caseHearings.add(generateCaseHearing(3));
        caseHearings.add(generateCaseHearing(4));
        caseHearings.add(generateCaseHearing(5));
        return caseHearings;
    }

    /**
     * generate Case Hearing for given case Status.
     *
     * @param caseStatus case Status
     * @return caseHearings list
     */
    private static List<CaseHearing> generateCaseHearings(String caseStatus) {
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(generateCaseHearing(caseStatus));
        return caseHearings;
    }

    /**
     * generate Case Hearings for given id.
     *
     * @param id counter for unique data gen
     * @return caseHearings list
     */
    private static CaseHearing generateCaseHearing(Integer id) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(2000000000L + id);
        caseHearing.setHearingType("hearingType" + id);
        caseHearing.setHearingDaySchedule(generateHearingDaySchedules());
        caseHearing.setRequestVersion(id);
        caseHearing.setHearingListingStatus("HEARING_REQUESTED");
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setListAssistCaseStatus("AWAITING_LISTING");
        caseHearing.setHmcStatus("LISTED");
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        return caseHearing;
    }

    /**
     * generate case hearing for given case status.
     *
     * @param caseStatus status
     * @return caseHearing case Hearing
     */
    private static CaseHearing generateCaseHearing(String caseStatus) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(2000000001L);
        caseHearing.setHearingType("hearingType1");
        caseHearing.setHearingDaySchedule(generateHearingDaySchedules());
        caseHearing.setRequestVersion(2);
        caseHearing.setHearingListingStatus(caseStatus);
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setListAssistCaseStatus(caseStatus);
        caseHearing.setHmcStatus(caseStatus);
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        return caseHearing;
    }

    private static List<HearingDaySchedule> generateHearingDaySchedules() {
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(generateHearingDaySchedule(1));
        hearingDaySchedules.add(generateHearingDaySchedule(2));
        hearingDaySchedules.add(generateHearingDaySchedule(3));
        hearingDaySchedules.add(generateHearingDaySchedule(4));
        return hearingDaySchedules;
    }

    private static HearingDaySchedule generateHearingDaySchedule(Integer id) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingRoomId("hearingRoom" + id);
        hearingDaySchedule.setHearingJudgeId("hearingJudge" + id);
        hearingDaySchedule.setHearingStartDateTime(LocalDateTime.now().plusDays(id));
        hearingDaySchedule.setHearingVenueId("hearingVenueId" + id);
        hearingDaySchedule.setHearingEndDateTime(LocalDateTime.now().plusDays(id).plusHours(2));
        hearingDaySchedule.setAttendees(generateAttendees());
        hearingDaySchedule.setListAssistSessionId("listAssistSessionId" + id);
        hearingDaySchedule.setPanelMemberId(PANEL_MEMBER_ID + id);
        return hearingDaySchedule;
    }

    private static List<Attendee> generateAttendees() {
        List<Attendee> attendees = new ArrayList<>();
        attendees.add(generateAttendee(1));
        attendees.add(generateAttendee(2));
        attendees.add(generateAttendee(3));
        attendees.add(generateAttendee(4));
        attendees.add(generateAttendee(5));
        attendees.add(generateAttendee(6));
        attendees.add(generateAttendee(7));
        return attendees;
    }

    private static Attendee generateAttendee(Integer id) {
        Attendee attendee = new Attendee();
        attendee.setHearingSubChannel("hearingSubCh" + id);
        attendee.setPartyId("partyId" + id);
        return attendee;
    }

    private static List<String> generatePanelMemberIds() {
        List<String> mbrIds = new ArrayList<>();
        mbrIds.add("memberId1");
        mbrIds.add("memberId2");
        mbrIds.add("memberId3");
        mbrIds.add("memberId4");
        mbrIds.add("memberId5");
        mbrIds.add("memberId6");
        mbrIds.add("memberId7");
        mbrIds.add("memberId8");
        return mbrIds;
    }

    private static DeleteHearingRequest generateDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCodes(List.of("REASONCODE25"));
        return  deleteHearingRequest;
    }

}
