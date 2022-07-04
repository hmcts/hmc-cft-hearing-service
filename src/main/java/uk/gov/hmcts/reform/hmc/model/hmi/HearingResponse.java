package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    @Size(max = 40, message = ValidationError.LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH)
    private String listAssistTransactionID;

    @JsonProperty("laCaseStatus")
    @EnumPattern(enumClass = ListAssistCaseStatus.class, fieldName = "laCaseStatus")
    private String laCaseStatus;

    @JsonProperty("listingStatus")
    @NotEmpty(message = ValidationError.HEARING_STATUS_CODE_NULL)
    @Size(max = 30, message = ValidationError.HEARING_STATUS_CODE_LENGTH)
    private String listingStatus;

    @JsonProperty("receivedDateTime")
    private LocalDateTime receivedDateTime;

    @JsonProperty("hearingCancellationReason")
    @Size(max = 40, message = ValidationError.HEARING_CANCELLATION_REASON_MAX_LENGTH)
    private String hearingCancellationReason;

    private List<HearingDaySchedule> hearingDaySchedule;

}
