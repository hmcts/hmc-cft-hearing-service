package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingCreatePostResponse {

    @NotEmpty(message = ValidationError.HEARING_REQUEST_ID_NULL_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{30}$", message = ValidationError.HEARING_REQUEST_ID_MAX_LENGTH_EMPTY_INVALID)
    protected String hearingRequestID;

    @NotEmpty(message = ValidationError.HEARING_STATUS_NULL_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{100}$", message = ValidationError.HEARING_STATUS_MAX_LENGTH_EMPTY_INVALID)
    protected String status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    protected LocalDateTime timeStamp;
}
