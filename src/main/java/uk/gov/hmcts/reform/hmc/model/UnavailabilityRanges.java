package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class UnavailabilityRanges {

    @NotNull(message = ValidationError.UNAVAILABLE_FROM_DATE_EMPTY)
    private LocalDate unavailableFromDate;

    @NotNull(message = ValidationError.UNAVAILABLE_TO_DATE_EMPTY)
    private LocalDate unavailableToDate;

    @JsonProperty("unavailabilityType")
    @EnumPattern(enumClass = DayOfWeekUnAvailableType.class, fieldName = "dowUnavailabilityType")
    private String unavailabilityType;
}
