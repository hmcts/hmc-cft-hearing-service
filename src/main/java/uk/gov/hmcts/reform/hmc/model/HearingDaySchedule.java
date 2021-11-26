package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingDaySchedule {

    private LocalDateTime hearingStartDateTime;

    private LocalDateTime hearingEndDateTime;

    @NotEmpty(message = ValidationError.LIST_ASSIST_SESSION_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.LIST_ASSIST_SESSION_ID_MAX_LENGTH)
    private String listAssistSessionID;

    @NotEmpty(message = ValidationError.HEARING_ROOM_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_ROOM_ID_MAX_LENGTH)
    private String hearingRoomId;

    @NotEmpty(message = ValidationError.HEARING_JUDGE_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_JUDGE_ID_MAX_LENGTH)
    private String hearingJudgeId;
}
