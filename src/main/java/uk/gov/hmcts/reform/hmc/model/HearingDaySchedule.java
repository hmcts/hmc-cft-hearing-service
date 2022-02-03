package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingDaySchedule {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingEndDateTime;

    @JsonProperty("listAssistSessionID")
    @Size(max = 60)
    @NotNull
    private String listAssistSessionId;

    @NotEmpty(message = ValidationError.HEARING_VENUE_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_VENUE_ID_MAX_LENGTH)
    private String hearingVenueId;

    @NotEmpty(message = ValidationError.HEARING_ROOM_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_ROOM_ID_MAX_LENGTH)
    private String hearingRoomId;

    @NotEmpty(message = ValidationError.HEARING_JUDGE_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_JUDGE_ID_MAX_LENGTH)
    private String hearingJudgeId;

    @Size(max = 60)
    private String panelMemberId;

    private List<Attendee> attendees;

}
