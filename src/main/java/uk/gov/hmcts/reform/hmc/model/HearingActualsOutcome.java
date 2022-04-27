package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import java.time.LocalDate;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingActualsOutcome implements Serializable {

    @NotEmpty(message = ValidationError.HA_OUTCOME_TYPE_NOT_EMPTY)
    @Size(max = 40, message = ValidationError.HA_OUTCOME_TYPE_MAX_LENGTH)
    private String hearingType;

    @NotNull(message = ValidationError.HA_OUTCOME_FINAL_FLAG_NOT_EMPTY)
    private Boolean hearingFinalFlag;

    @NotNull(message = ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY)
    private String hearingResult;

    @Size(max = 70, message = ValidationError.HA_OUTCOME_REASON_TYPE_MAX_LENGTH)
    private String hearingResultReasonType;

    @PastOrPresent
    @NotNull(message = ValidationError.HA_OUTCOME_REQUEST_DATE_NOT_EMPTY)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingResultDate;
}
