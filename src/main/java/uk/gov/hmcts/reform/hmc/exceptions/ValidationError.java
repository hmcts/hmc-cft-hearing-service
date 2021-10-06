package uk.gov.hmcts.reform.hmc.exceptions;

public class ValidationError {

    public static final String HEARING_TYPE_EMPTY = "Hearing type can not be empty";
    public static final String HEARING_WINDOW_EMPTY = "Hearing window can not be empty";
    public static final String DURATION_EMPTY = "Duration can not be empty";
    public static final String NON_STANDARD_HEARING_DURATION_REASONS = "Non standard hearing duration reason length "
        + "cannot be greater than 70";
    public static final String HEARING_PRIORITY_TYPE = "Hearing priority type can not be empty";
    public static final String HEARING_PRIORITY_TYPE_MAX_LENGTH = "Hearing priority type must not be more than "
        + "60 characters long";
    public static final String HEARING_LOCATION_EMPTY = "Hearing location can not be empty";
    public static final String FACILITY_TYPE_MAX_LENGTH = "Facility type must not be more than 70 characters long";
    public static final String LISTING_COMMENTS_MAX_LENGTH = "Listing comments must not be more than "
        + "5000 characters long";
    public static final String HEARING_REQUESTER_MAX_LENGTH = "Hearing requester must not be more than 60 "
        + "characters long";

    private ValidationError() {
    }
}
