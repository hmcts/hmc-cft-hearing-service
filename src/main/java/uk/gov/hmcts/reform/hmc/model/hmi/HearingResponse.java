package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    @Size(max = 40, message = ValidationError.LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH)
    private String listAssistTransactionID;

    @JsonProperty("laCaseStatus")
    private String laCaseStatus;

    @JsonProperty("listingStatus")
    private String listingStatus;

    @JsonProperty("receivedDateTime")
    private LocalDateTime receivedDateTime;

    @JsonProperty("responseVersion")
    private Long responseVersion;

    @JsonProperty("hearingCancellationReason")
    @Size(max = 40, message = ValidationError.HEARING_CANCELLATION_REASON_MAX_LENGTH)
    private String hearingCancellationReason;

    private List<HearingDaySchedule> hearingDaySchedule;

}
