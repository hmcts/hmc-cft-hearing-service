package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Hearing {

    private String listingRequestId;

    @NotNull(message = ValidationError.HEARING_CASE_VERSION_ID_NULL)
    private Integer hearingCaseVersionId;

    private String hearingCaseIdHmcts;

    private Object hearingCaseJurisdiction;

    @NotNull(message = ValidationError.HEARING_CASE_STATUS_NULL)
    private HearingCaseStatus hearingCaseStatus;

    private String hearingIdCaseHQ;

    private Object hearingType;

    private HearingStatus hearingStatus;

    @Size(max = 70, message = ValidationError.HEARING_CANCELLATION_REASON_LENGTH)
    private String hearingCancellationReason;

    private LocalDateTime hearingStartTime;

    private LocalDateTime hearingEndTime;

    private boolean hearingPrivate;

    private boolean hearingRisk;

    private boolean hearingTranslatorRequired;

    private LocalDateTime hearingCreatedDate;

    private String hearingCreatedBy;

    private HearingVenue hearingVenue;

    private HearingRoom hearingRoom;

    private HearingResponse hearingAttendee;

    private HearingJoh hearingJoh;

    private Object hearingSession;
}
