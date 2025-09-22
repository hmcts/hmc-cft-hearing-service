package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class HearingActualsOutcome implements Serializable {

    @NotEmpty(message = ValidationError.HA_OUTCOME_TYPE_NOT_EMPTY)
    @Size(max = 40, message = ValidationError.HA_OUTCOME_TYPE_MAX_LENGTH)
    private String hearingType;

    private Boolean hearingFinalFlag;

    private String hearingResult;

    @Size(max = 70, message = ValidationError.HA_OUTCOME_REASON_TYPE_MAX_LENGTH)
    private String hearingResultReasonType;

    @PastOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingResultDate;

    public boolean isEmpty() {
        return (hearingType == null || hearingType.isEmpty())
            && hearingFinalFlag == null
            && hearingResult == null
            && (hearingResultReasonType == null || hearingResultReasonType.isEmpty())
            && hearingResultDate == null;
    }
}
