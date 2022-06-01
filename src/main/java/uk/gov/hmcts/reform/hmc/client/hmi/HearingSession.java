package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingSession {

    private String hearingCaseHQ;
    @Size(max = 40, message = ValidationError.HEARING_ATTENDEE_ENTITY_ID_LENGTH)
    private JsonNode hearingType;
    private HearingStatus hearingStatus;
    private LocalDateTime hearingStartTime;
    private LocalDateTime hearingEndTime;
    private Integer hearingSequence;
    private Boolean hearingPrivate;
    private Boolean hearingRisk;
    private Boolean hearingTranslatorRequired;
    private HearingVenue hearingVenue;
    private HearingRoom hearingRoom;
    private String hearingVhStatus;
    private String hearingVhId;
    private List<HearingAttendee> hearingAttendees;
    private List<HearingJoh> hearingJohs;

}
