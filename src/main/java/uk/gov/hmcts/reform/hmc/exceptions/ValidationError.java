package uk.gov.hmcts.reform.hmc.exceptions;

public final class ValidationError {
    public static final String AUTO_LIST_FLAG_NULL_EMPTY = "Auto list flag can not be null or empty";
    public static final String HEARING_TYPE_NULL_EMPTY = "Hearing type can not be null or empty";
    public static final String HEARING_TYPE_MAX_LENGTH = "Hearing type must not be more than 40 characters long";
    public static final String HEARING_WINDOW_NULL = "Hearing window can not be null";
    public static final String DURATION_EMPTY = "Duration can not be empty";
    public static final String DURATION_MIN_LENGTH = "Duration can not be less than 1";
    public static final String NON_STANDARD_HEARING_DURATION_REASONS = "Non standard hearing duration reason length "
        + "cannot be greater than 70";
    public static final String HEARING_PRIORITY_TYPE = "Hearing priority type can not be empty";
    public static final String HEARING_PRIORITY_TYPE_MAX_LENGTH = "Hearing priority type must not be more than "
        + "60 characters long";
    public static final String DURATION_MIN_VALUE = "Duration should be greater than or equal to 1";
    public static final String NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE = "Duration should be greater than or equal to 0";
    public static final String HEARING_LOCATION_EMPTY = "Hearing locations can not be empty";
    public static final String FACILITY_TYPE_MAX_LENGTH = "Facility type must not be more than 70 characters long";
    public static final String LISTING_COMMENTS_MAX_LENGTH = "Listing comments must not be more than "
        + "5000 characters long";
    public static final String HEARING_REQUESTER_MAX_LENGTH = "Hearing requester must not be more than 60 "
        + "characters long";
    public static final String LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH = "Lead judge contract type must not be more than "
        + "70 characters long";
    public static final String MEMBER_ID_EMPTY = "Member Id can not be empty";
    public static final String MEMBER_ID_MAX_LENGTH = "Member Id must not be more than 70 characters long";
    public static final String MEMBER_TYPE_MAX_LENGTH = "Member type must not be more than 70 characters long";
    public static final String LOCATION_TYPE_EMPTY = "Location type can not be empty";
    public static final String HMCTS_SERVICE_CODE_EMPTY = "Hmcts service code can not be empty";
    public static final String CASE_REF_EMPTY = "Case ref can not be empty";
    public static final String REQUEST_TIMESTAMP_EMPTY = "Request time stamp can not be empty";
    public static final String EXTERNAL_CASE_REFERENCE_MAX_LENGTH = "External case reference must not be more than "
        + "70 characters long";
    public static final String CASE_DEEP_LINK_EMPTY = "Case deep link can not be empty";
    public static final String CASE_DEEP_LINK_MAX_LENGTH = "Case deep link can not be more than 1024 characters long";
    public static final String HMCTS_INTERNAL_CASE_NAME_EMPTY = "Hmcts internal case name can not be empty";
    public static final String HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH = "Hmcts internal case name can not be more than "
        + "1024 characters long";
    public static final String PUBLIC_CASE_NAME_EMPTY = "Public case name can not be empty";
    public static final String PUBLIC_CASE_NAME_MAX_LENGTH = "Public case name can not be more than 1024 "
        + "characters long";
    public static final String CASE_MANAGEMENT_LOCATION_CODE_EMPTY = "Case management location can not be empty";
    public static final String CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH = "Case management location can not be more "
        + "than 40 characters long";
    public static final String CASE_SLA_START_DATE_EMPTY = "Case sla start date can not be empty";
    public static final String INVALID_HEARING_DETAILS = "Hearing Details are required";
    public static final String INVALID_CASE_DETAILS = "Case details are required";

    private ValidationError() {
    }
}
