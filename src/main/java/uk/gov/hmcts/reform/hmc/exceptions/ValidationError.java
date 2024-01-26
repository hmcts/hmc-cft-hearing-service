package uk.gov.hmcts.reform.hmc.exceptions;

public final class ValidationError {

    private ValidationError() {
    }

    private static final String CHARACTERS_LONG = "characters long";
    public static final String AUTO_LIST_FLAG_NULL_EMPTY = "Auto list flag can not be null or empty";
    public static final String MUST_BE_FALSE_IF_YOU_SUPPLY_A_CHANGE_REASONCODE =
        "001 autoListFlag must be FALSE if you supply a change reasoncode";
    public static final String HEARING_TYPE_NULL_EMPTY = "Hearing type can not be null or empty";
    public static final String HEARING_TYPE_MAX_LENGTH = "Hearing type must not be more than 40 " + CHARACTERS_LONG;
    public static final String DURATION_EMPTY = "Duration can not be empty";
    public static final String NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG = "Non standard hearing duration "
        + "reasons length cannot be greater than 70 " + CHARACTERS_LONG;
    public static final String HEARING_PRIORITY_TYPE = "Hearing priority type can not be empty";
    public static final String HEARING_PRIORITY_TYPE_MAX_LENGTH = "Hearing priority type must not be more than 60 "
        + CHARACTERS_LONG;
    public static final String DURATION_MIN_VALUE = "Duration should be greater than or equal to 0";
    public static final String NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE = "Number of physical attendees should be "
        + " greater than or equal to 0";
    public static final String HEARING_LOCATION_EMPTY = "Hearing locations can not be empty";
    public static final String LISTING_COMMENTS_MAX_LENGTH = "Listing comments must not be more than 2000 "
        + CHARACTERS_LONG;
    public static final String LISTING_REASON_CODE_MAX_LENGTH = "Listing comments must not be more than 70 "
        + CHARACTERS_LONG;
    public static final String HEARING_REQUESTER_MAX_LENGTH = "Hearing requester must not be more than 60 "
        + CHARACTERS_LONG;
    public static final String LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH = "Lead judge contract type must not be more than "
        + "70 " + CHARACTERS_LONG;
    public static final String MEMBER_ID_EMPTY = "Member Id can not be empty";
    public static final String MEMBER_ID_MAX_LENGTH = "Member Id must not be more than 70 " + CHARACTERS_LONG;
    public static final String MEMBER_TYPE_MAX_LENGTH = "Member type must not be more than 70 " + CHARACTERS_LONG;
    public static final String LOCATION_ID_EMPTY = "Location id can not be empty";
    public static final String HMCTS_SERVICE_CODE_EMPTY_INVALID = "Hmcts service code is invalid";
    public static final String CASE_REF_EMPTY = "Case ref can not be empty";
    public static final String CASE_REF_INVALID = "Case ref details is invalid";
    public static final String CASE_REF_INVALID_LENGTH = "Case ref has invalid length";
    public static final String EXTERNAL_CASE_REFERENCE_MAX_LENGTH = "External case reference must not be more than 70 "
        + CHARACTERS_LONG;
    public static final String CASE_DEEP_LINK_EMPTY = "Case deep link can not be empty";
    public static final String CASE_DEEP_LINK_INVALID = "Case deep link is invalid";
    public static final String CASE_DEEP_LINK_MAX_LENGTH = "Case deep link can not be more than 1024 "
        + CHARACTERS_LONG;
    public static final String HMCTS_INTERNAL_CASE_NAME_EMPTY = "Hmcts internal case name can not be empty";
    public static final String HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH = "Hmcts internal case name can not be more than  "
        + "1024 " + CHARACTERS_LONG;
    public static final String PUBLIC_CASE_NAME_EMPTY = "Public case name can not be empty";
    public static final String PUBLIC_CASE_NAME_MAX_LENGTH = "Public case name can not be more than 1024 "
        + CHARACTERS_LONG;
    public static final String CASE_MANAGEMENT_LOCATION_CODE_EMPTY = "Case management location can not be empty";
    public static final String CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH = "Case management location can not be more "
        + "than 40 " + CHARACTERS_LONG;
    public static final String CASE_SLA_START_DATE_EMPTY = "Case sla start date can not be empty";
    public static final String INVALID_HEARING_DETAILS = "Hearing Details are required";
    public static final String INVALID_REQUEST_DETAILS = "Request details are required";
    public static final String INVALID_CASE_DETAILS = "Case details are required";
    public static final String PARTY_DETAILS_NULL_EMPTY = "Party id can not be empty";
    public static final String PARTY_DETAILS_MAX_LENGTH = "Party id must not be more than 40 " + CHARACTERS_LONG;
    public static final String PARTY_ROLE_MAX_LENGTH = "Party role must not be more than 40 " + CHARACTERS_LONG;
    public static final String NAME_NULL_EMPTY = "Name can not be empty";
    public static final String NAME_MAX_LENGTH = "Name must not be more than 2000 " + CHARACTERS_LONG;
    public static final String ORGANISATION_TYPE_NULL_EMPTY = "Organisation type can not be empty";
    public static final String ORGANISATION_TYPE_MAX_LENGTH = "Organisation type must not be more than 60 "
        + CHARACTERS_LONG;
    public static final String CFT_ORG_ID_MAX_LENGTH = "CFT organisation id must not be more than 60 "
        + CHARACTERS_LONG;
    public static final String UNAVAILABLE_FROM_DATE_EMPTY = "Unavailable from date can not be empty";
    public static final String UNAVAILABLE_TO_DATE_EMPTY = "Unavailable to date can not be empty";
    public static final String RELATED_PARTY_EMPTY = "Related Party can not be empty";
    public static final String RELATED_PARTY_MAX_LENGTH = "Related Party must not be more than 15 "
        + CHARACTERS_LONG;
    public static final String RELATIONSHIP_TYPE_EMPTY = "Relationship type can not be empty";
    public static final String RELATIONSHIP_TYPE_MAX_LENGTH = "Relationship type must not be more than 10 characters"
        + " long";
    public static final String TITLE_MAX_LENGTH = "Title must not be more than 40 " + CHARACTERS_LONG;
    public static final String FIRST_NAME_EMPTY = "First name can not be empty";
    public static final String FIRST_NAME_MAX_LENGTH = "First name must not be more than 100 " + CHARACTERS_LONG;
    public static final String LAST_NAME_EMPTY = "Last name can not be empty";
    public static final String LAST_NAME_MAX_LENGTH = "Last name must not be more than 730 " + CHARACTERS_LONG;
    public static final String MISSING_CHANNEL_TYPE = "Hearing Channel Type can not be empty or null";
    public static final String HEARING_CHANNEL_EMPTY = "Hearing Channel must be present and not null or empty";
    public static final String NON_UNIQUE_CHANNEL_TYPE = "Non-Unique Channel Type";
    public static final String CHANNEL_TYPE_MAX_LENGTH = "Channel Type must not be more than 70 " + CHARACTERS_LONG;
    public static final String PREFERRED_HEARING_CHANNEL_MAX_LENGTH = "Preferred hearing channel must not be more than "
        + "70 " + CHARACTERS_LONG;
    public static final String INTERPRETER_LANGUAGE_MAX_LENGTH = "Interpreter language must not be more than 10 "
        + CHARACTERS_LONG;
    public static final String REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG = "Reasonable adjustments must not be more than "
        + "10 " + CHARACTERS_LONG;
    public static final String VULNERABLE_DETAILS_MAX_LENGTH = "Vulnerable details must not be more than 2000 "
        + CHARACTERS_LONG;
    public static final String HEARING_CHANNEL_EMAIL_MAX_LENGTH = "Hearing channel email must not be more than 120 "
        + CHARACTERS_LONG;
    public static final String HEARING_CHANNEL_EMAIL_INVALID = "HearingChannelEmail must be valid email address";
    public static final String HEARING_CHANNEL_PHONE_MAX_LENGTH = "Hearing channel phone must not be more than 30 "
        + CHARACTERS_LONG;
    public static final String HEARING_CHANNEL_PHONE_INVALID = "HearingChannel phone is invalid";
    public static final String ROLE_TYPE_MAX_LENGTH_MSG = "Role type length cannot be greater than 70 "
        + CHARACTERS_LONG;
    public static final String AUTHORISATION_TYPE_MAX_LENGTH_MSG = "Authorisation type length cannot be greater "
        + "than 70 " + CHARACTERS_LONG;
    public static final String AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG = "Authorisation sub type length cannot be "
        + "greater than 70 " + CHARACTERS_LONG;
    public static final String PANEL_SPECIALISMS_MAX_LENGTH_MSG = "Panel Specialisms length cannot be greater than 70 "
        + CHARACTERS_LONG;
    public static final String FACILITIES_REQUIRED_MAX_LENGTH_MSG = "Facilities required length cannot be greater "
        + "than 70 " + CHARACTERS_LONG;
    public static final String INVALID_PANEL_REQUIREMENTS = "Panel requirements are required";
    public static final String CATEGORY_VALUE = "category Value cannot be greater than 70 " + CHARACTERS_LONG;
    public static final String HEARING_WINDOW_EMPTY_NULL = "Hearing window details are required";
    public static final String HEARING_WINDOW_DETAILS_ARE_INVALID = "Hearing window details are invalid";
    public static final String INVALID_HEARING_LOCATION = "Hearing locations are required";
    public static final String INVALID_CASE_CATEGORIES = "Case categories are required";
    public static final String CASE_CATEGORY_EMPTY = "Case category can not be empty";
    public static final String CASE_RESTRICTED_FLAG_NULL_EMPTY = "Case restricted flag can not be empty";
    public static final String INVALID_HEARING_REQUEST_DETAILS = "Invalid details";
    public static final String INVALID_ORG_INDIVIDUAL_DETAILS = "Either Individual or Organisation details should "
        + "be present";
    public static final String INVALID_CANCELLATION_REASON_CODE = "Cancellation Reason code details are not present";
    public static final String CANCELLATION_REASON_CODE_MAX_LENGTH_MSG = "Cancellation Reason code "
        + "length must be at least 1 and no greater than 100 characters";
    public static final String INVALID_VERSION_NUMBER = "Invalid version number";
    public static final String INVALID_HEARING_ID_DETAILS = "Invalid hearing Id";
    public static final String CASE_NOT_FOUND = "Case could not be found";
    public static final String VERSION_NUMBER_NULL_EMPTY = "Version number must be present";
    public static final String INVALID_DELETE_HEARING_STATUS = "Invalid hearing state for DELETE";
    public static final String INVALID_PUT_HEARING_STATUS = "Invalid hearing state for PUT";

    public static final String CATEGORY_TYPE_EMPTY = "Category type must be present";
    public static final String CATEGORY_VALUE_EMPTY = "Category value must be present";
    public static final String PARTY_TYPE_EMPTY = "Party type must be present";
    public static final String PARTY_ROLE_EMPTY = "Party role must be present";
    public static final String HMCTS_SERVICE_CODE_EMPTY = "Hmcts service code can not be empty";
    public static final String INVALID_HMCTS_SERVICE_CODE = "Invalid hmcts service code";

    public static final String HEARING_ID_NOT_FOUND = "No hearing found for reference: %s";
    public static final String HEARING_GROUP_ID_NOT_FOUND = "No hearing group found for reference: %s";
    public static final String INVALID_DELETE_HEARING_GROUP_STATUS = "007 group is in a %s state";
    public static final String INVALID_DELETE_HEARING_GROUP_HEARING_STATUS = "008 Invalid state for unlinking hearing"
        + " request %s";
    public static final String PARTIES_NOTIFIED_ID_NOT_FOUND = "001 No such id: %s";
    public static final String PARTIES_NOTIFIED_NO_SUCH_RESPONSE = "001 No such response";
    public static final String PARTIES_NOTIFIED_ALREADY_SET = "003 Already set";

    public static final String HEARING_ACTUALS_ID_NOT_FOUND = "001 No such id: %s";
    public static final String HEARING_ACTUALS_INVALID_STATUS = "002 invalid status %s";
    public static final String HEARING_ACTUALS_HEARING_DAYS_INVALID = "003 invalid date";
    public static final String HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS = "004 non-unique dates";
    public static final String HEARING_ACTUALS_MISSING_RESULT_TYPE = "%s result requires a hearingResultReasonType";
    public static final String HEARING_ACTUALS_MISSING_HEARING_OUTCOME = "005 missing hearing outcome";
    public static final String HEARING_ACTUALS_MISSING_HEARING_DAY = "003 missing hearing day actuals";
    public static final String HEARING_ACTUALS_UN_EXPECTED = "004 unexpected hearing day actuals";
    public static final String HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND = "No response found on the hearing %s";

    public static final String HA_OUTCOME_DETAIlS_NOT_VALID = "hearingOutcome details not valid";
    public static final String HA_OUTCOME_TYPE_NOT_EMPTY = "hearingType cannot be null or empty";
    public static final String HA_OUTCOME_TYPE_MAX_LENGTH = "Hearing type must not be more than 40 " + CHARACTERS_LONG;
    public static final String HA_OUTCOME_FINAL_FLAG_NOT_EMPTY = "hearingFinalFlag has to be provided";
    public static final String HA_OUTCOME_RESULT_NOT_EMPTY = "hearingResult has to be one of"
        + " COMPLETED, ADJOURNED, CANCELLED";
    public static final String HA_OUTCOME_REASON_TYPE_MAX_LENGTH = "hearingResultReasonType must not be more than 70 "
        + CHARACTERS_LONG;
    public static final String HA_OUTCOME_REQUEST_DATE_NOT_EMPTY = "hearingResultDate cannot be null or empty";

    public static final String HA_OUTCOME_REQUEST_DATE_MUST_BE_PAST_OR_PRESENT =
        "hearingResultDate must be past or present";

    public static final String HA_HEARING_DAY_HEARING_DATE_NOT_EMPTY = "hearingDate cannot be null or empty";
    public static final String HA_HEARING_DAY_PAUSE_START_TIME_NOT_EMPTY = "pauseStartTime cannot be null or empty";
    public static final String HA_HEARING_DAY_PAUSE_END_TIME_DATE_NOT_EMPTY = "pauseEndTime cannot be null or empty";

    public static final String HA_HEARING_DAY_PARTY_ID_MAX_LENGTH = "actualPartyId must not be more than 40 "
        + CHARACTERS_LONG;
    public static final String HA_HEARING_DAY_PARTY_ROLE_NOT_EMPTY = "partyRole cannot be null or empty";
    public static final String HA_HEARING_DAY_PARTY_ROLE_MAX_LENGTH = "partyRole must not be more than 40 "
        + CHARACTERS_LONG;
    public static final String HA_HEARING_DAY_PARTY_CHANNEL_NOT_EMPTY = "partyChannelSubType cannot be null or empty";
    public static final String HA_HEARING_DAY_PARTY_CHANNEL_MAX_LENGTH = "partyChannelSubType must not be more than 40 "
        + CHARACTERS_LONG;
    public static final String HA_HEARING_DAY_REPRESENTED_PARTY_MAX_LENGTH = "representedParty must not be more than"
        + " 40 " + CHARACTERS_LONG;

    public static final String HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_NOT_EMPTY = "firstName cannot be null or empty";
    public static final String HA_HEARING_DAY_INDIVIDUAL_FIRST_NAME_MAX_LENGTH = "firstName must not be more than 40 "
        + CHARACTERS_LONG;
    public static final String HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_NOT_EMPTY = "lastName cannot be null or empty";
    public static final String HA_HEARING_DAY_INDIVIDUAL_LAST_NAME_MAX_LENGTH = "lastName must not be more than 40 "
        + CHARACTERS_LONG;

    public static final String HA_HEARING_DAY_ORGANISATION_NAME_NOT_EMPTY = "organisation name cannot be null or empty";
    public static final String HA_HEARING_DAY_ORGANISATION_NAME_MAX_LENGTH = "organisation name must not be more than"
        + " 40 " + CHARACTERS_LONG;

    public static final String OTHER_REASON_LENGTH = "Other reasonable adjustment details cannot be greater than 3000 "
        + CHARACTERS_LONG;
    public static final String CUSTODY_STATUS_LENGTH = "Custody status cannot be greater than 80 " + CHARACTERS_LONG;
    public static final String HEARING_ID_LENGTH = "Hearing id cannot be greater than 30 " + CHARACTERS_LONG;
    public static final String HEARING_ID_EMPTY = "Hearing id can not be null or empty";
    public static final String GROUP_REASON_LENGTH = "Group reason cannot be greater than 8 " + CHARACTERS_LONG;
    public static final String GROUP_NAME_LENGTH = "Group name cannot be greater than 255 " + CHARACTERS_LONG;
    public static final String GROUP_COMMENTS_LENGTH = "Group comments cannot be greater than 4000 " + CHARACTERS_LONG;
    public static final String GROUP_LINK_TYPE_EMPTY = "Group link type can not be null or empty";
    public static final String GROUP_REASON_EMPTY = "Group reason can not be null or empty";
    public static final String GROUP_DETAILS = "Group details are required";
    public static final String HEARINGS_IN_GROUP = "Linked hearing details are required";
    public static final String HEARINGS_IN_GROUP_SIZE = "001 Insufficient requestIds";

    public static final String HEARING_REQUEST_ID_MAX_LENGTH =
        "Hearing request id length cannot be greater than 30 " + CHARACTERS_LONG;
    public static final String HEARING_GROUP_REQUEST_ID_MAX_LENGTH =
        "Hearing request id length cannot be greater than 30 " + CHARACTERS_LONG;
    public static final String LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH =
        "List assist transaction id length cannot be greater than 40 " + CHARACTERS_LONG;
    public static final String HEARING_CANCELLATION_REASON_MAX_LENGTH =
        "Hearing cancellation reason length cannot be greater than 40 " + CHARACTERS_LONG;


    public static final String INVALID_LINKED_GROUP_REQUEST_ID_DETAILS = "Invalid linked group id";
    public static final String INVALID_STATE_FOR_HEARING_REQUEST
        = "004 Invalid state for hearing request <hearingId>";
    public static final String INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST
        = "008 Invalid state for unlinking hearing request <hearingId>";
    public static final String HEARING_REQUEST_CANNOT_BE_LINKED = "002 hearing request isLinked is False";
    public static final String INVALID_STATE_FOR_LINKED_GROUP = "007 group is in a <state> state";
    public static final String HEARING_REQUEST_ALREADY_LINKED = "003 hearing request already in a group";
    public static final String INVALID_HEARING_ORDER = "Hearing order must exist and be greater than 0";
    public static final String INVALID_GROUP_LINK_TYPE = "Invalid value for GroupLinkType <linkType>";
    public static final String HEARING_ORDER_NOT_UNIQUE = "005 Hearing Order is not unique";

    public static final String VENUE_LOCATION_CODE_NULL = "Venue location code can not be null or empty";
    public static final String VENUE_LOCATION_CODE_LENGTH = "Venue location code must not be more than 30 "
        + CHARACTERS_LONG;
    public static final String TRANSACTION_ID_CASE_HQ_NULL = "Transaction Id Case Hq can not be null or empty";
    public static final String TRANSACTION_ID_CASE_HQ_LENGTH = "Transaction Id Case Hq must not be more than 60 "
        + CHARACTERS_LONG;
    public static final String TRANSACTION_ID_CASE_HQ_EMPTY = "Timestamp Id Case Hq can not be null or empty";
    public static final String HEARING_STATUS_CODE_NULL = "Hearing status code can not be null or empty";
    public static final String HEARING_STATUS_CODE_LENGTH = "Hearing status code must not be more than 30 "
        + CHARACTERS_LONG;
    public static final String HEARING_LISTING_STATUS_CODE_LENGTH = "Hearing Listing status code must not be more "
        + "than 30 " + CHARACTERS_LONG;
    public static final String HEARING_ROOM_NAME_LENGTH = "Hearing room name must not be more than 70 "
        + CHARACTERS_LONG;
    public static final String HEARING_JOH_CODE_LENGTH = "Hearing joh code must not be more than 30 "
        + CHARACTERS_LONG;
    public static final String HEARING_CODE_NULL = "Hearing code can not be null or empty";
    public static final String HEARING_ATTENDEE_ENTITY_ID_LENGTH =
        "Hearing attendee entity id must not be more than 40 " + CHARACTERS_LONG;
    public static final String HEARING_CASE_VERSION_ID_NULL = "Hearing case version id can not be null or empty";
    public static final String HEARING_CASE_STATUS_NULL = "Hearing case status not be null or empty";
    public static final String HEARING_CANCELLATION_REASON_LENGTH =
        "Hearing cancellation reason must not be more than 70 " + CHARACTERS_LONG;

    public static final String HEARING_RESPONSE_DATETIME_NULL = "Hearing response received date "
        + "time can not be null or empty";
    public static final String HEARING_BROADCAST_DATETIME_NULL = "Hearing response broadcast date "
        + "time can not be null or empty";
    public static final String HMCTS_STATUS_NULL = "HMCTS status can not be null or empty";
    public static final String HMCTS_STATUS_LENGTH = "HMCTS status must not be more than 100 " + CHARACTERS_LONG;
    public static final String LISTING_STATUS_NULL = "Listing status can not be null or empty";
    public static final String REJECTED_BY_LIST_ASSIST = "005 rejected by List Assist";
    public static final String LIST_ASSIST_FAILED_TO_RESPOND = "006 List Assist failed to respond";
    public static final String LIST_ASSIST_CASE_STATUS_NULL = "List assist case status can not be null or empty";
    public static final String AMEND_REASON_CODE_MAX_LENGTH = "Amend reason code must be at least 1 but no more than "
        + "70 " + CHARACTERS_LONG;
    public static final String INVALID_AMEND_REASON_CODE = "Amend reason code details are required";

    public static final String INTERNAL_SERVER_ERROR = "Error occurred on the server";

    public static final String MISSING_INDIVIDUAL_DETAILS = "Individual details are missing";
    public static final String MISSING_ORGANISATION_DETAILS = "Organisation details are missing";
    public static final String INVALID_CASE_REFERENCE = "Invalid case reference";
    public static final String HMCTS_DEPLOYMENT_ID_NOT_REQUIRED = "HMCTS deployment id is not required";
    public static final String HMCTS_DEPLOYMENT_ID_MAX_LENGTH = "HMCTS deployment id must not be more than 40 "
        + CHARACTERS_LONG;
}
