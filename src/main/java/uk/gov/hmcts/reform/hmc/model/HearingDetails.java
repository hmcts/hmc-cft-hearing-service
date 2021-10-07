package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.HearingDurationReasonMaxLengthConstraint;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingDetails {

    @NotNull(message = ValidationError.AUTO_LIST_FLAG_NULL_EMPTY)
    private Boolean autoListFlag;

    @NotEmpty(message = ValidationError.HEARING_TYPE_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.HEARING_TYPE_MAX_LENGTH)
    private String hearingType;

    @NotNull(message = ValidationError.HEARING_WINDOW_NULL)
    private HearingWindow hearingWindow;

    @NotNull(message = ValidationError.DURATION_EMPTY)
    @Min(value = 1, message = "Duration should be greater than or equal to 1")
    private Integer duration;

    @HearingDurationReasonMaxLengthConstraint
    private List<String> nonStandardHearingDurationReasons;

    /*@NotEmpty(message = ValidationError.HEARING_PRIORITY_TYPE)
    @Size(max = 60, message = ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH)
    private String hearingPriorityType;

    private Integer numberOfPhysicalAttendees;

    private Boolean hearingInWelshFlag;

    @NotNull(message = ValidationError.HEARING_LOCATION_EMPTY)
    private HearingLocation[] hearingLocations;

    @Size(max = 70, message = ValidationError.FACILITY_TYPE_MAX_LENGTH)
    private String[] facilitiesRequired;

    @Size(max = 5000, message = ValidationError.LISTING_COMMENTS_MAX_LENGTH)
    private String listingComments;

    @Size(max = 60, message = ValidationError.HEARING_REQUESTER_MAX_LENGTH)
    private String hearingRequester;

    private Boolean privateHearingRequiredFlag = true;

    @Size(max = 70, message = ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH)
    private String leadJudgeContractType;

    private PanelRequirements panelRequirements;

    private Boolean hearingIsLinkedFlag = false;*/

}
