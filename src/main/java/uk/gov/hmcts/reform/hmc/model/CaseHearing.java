package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CaseHearing {

    @NotEmpty(message = ValidationError.HEARING_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_ID_MAX_LENGTH)
    private String hearingID;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingRequestDateTime;

    @NotEmpty(message = ValidationError.HEARING_TYPE_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_TYPE_MAX_LENGTH)
    private String hearingType;

    @NotEmpty(message = ValidationError.HMC_STATUS_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HMC_STATUS_MAX_LENGTH)
    private String hmcStatus;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime lastResponseReceivedDateTime;

    @NotEmpty(message = ValidationError.RESPONSE_VERSION_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.RESPONSE_VERSION_MAX_LENGTH)
    private String responseVersion;

    @NotEmpty(message = ValidationError.HEARING_LISTING_STATUS_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.HEARING_LISTING_STATUS_MAX_LENGTH)
    private String hearingListingStatus;

    @NotEmpty(message = ValidationError.LIST_ASSIST_CASE_STATUS_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.LIST_ASSIST_CASE_STATUS_MAX_LENGTH)
    private String lstAssistCaseStatus;

    private HearingDaySchedule hearingDaySchedule;
}
