package uk.gov.hmcts.reform.hmc.model;

import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HearingDetails {

    private boolean autoListFlag;

    @NotEmpty(message = ValidationError.HEARING_TYPE_EMPTY)
    private String hearingType;

    @NotNull(message = ValidationError.HEARING_WINDOW_EMPTY)
    private HearingWindow hearingWindow;

    @NotNull(message = ValidationError.DURATION_EMPTY)
    private Integer duration;

    @Size(max = 70, message = ValidationError.NON_STANDARD_HEARING_DURATION_REASONS)
    private String[] nonStandardHearingDurationReasons;

    @NotEmpty(message = ValidationError.HEARING_PRIORITY_TYPE)
    @Size(max = 60, message = ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH)
    private String hearingPriorityType;

    private Integer numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    @NotNull(message = ValidationError.HEARING_LOCATION_EMPTY)
    //private HearingLocation hearingLocation;

    @Size(max = 70, message = ValidationError.FACILITY_TYPE_MAX_LENGTH)
    private String facilityType;

    @Size(max = 5000, message = ValidationError.LISTING_COMMENTS_MAX_LENGTH)
    private String listingComments;

    @Size(max = 60, message = ValidationError.HEARING_REQUESTER_MAX_LENGTH)
    private String hearingRequester;

    private Boolean privateHearingRequiredFlag = true;
}
