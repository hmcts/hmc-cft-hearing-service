package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Hearing {

    private String listingRequestId;

    @NotNull(message = ValidationError.HEARING_CASE_VERSION_ID_NULL)
    private Integer hearingCaseVersionId;

    @JsonProperty("hearingCaseIdHMCTS")
    private String hearingCaseIdHmcts;

    private JsonNode hearingCaseJurisdiction;

    @NotNull(message = ValidationError.HEARING_CASE_STATUS_NULL)
    @Valid
    private HearingCaseStatus hearingCaseStatus;

    private String hearingIdCaseHQ;

    private JsonNode hearingType;

    @Valid
    private HearingStatus hearingStatus;

    @Size(max = 70, message = ValidationError.HEARING_CANCELLATION_REASON_LENGTH)
    private String hearingCancellationReason;

    private LocalDateTime hearingStartTime;

    private LocalDateTime hearingEndTime;

    private Boolean hearingPrivate;

    private Boolean hearingRisk;

    private Boolean hearingTranslatorRequired;

    private LocalDateTime hearingCreatedDate;

    private String hearingCreatedBy;

    private HearingVenue hearingVenue;

    private HearingRoom hearingRoom;

    private String hearingVhStatus;

    private String hearingVhId;

    private String hearingVhGroupId;

    private List<HearingAttendee> hearingAttendees;

    private List<HearingJoh> hearingJohs;

    private List<HearingSession> hearingSessions;
}
