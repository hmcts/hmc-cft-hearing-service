package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RequestDetails {

    @JsonProperty("hearingRequestID")
    @Size(max = 30, message = ValidationError.HEARING_REQUEST_ID_MAX_LENGTH)
    private String hearingRequestId;

    private String status;

    private LocalDateTime timestamp;

    private int versionNumber;

    @Size(max = 30, message = ValidationError.HEARING_GROUP_REQUEST_ID_MAX_LENGTH)
    private String hearingGroupRequestId;

    private LocalDateTime partiesNotified;

    private List<String> cancellationReasonCodes;
}
