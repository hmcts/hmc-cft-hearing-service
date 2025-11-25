package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UnavailabilityRanges {

    @NotNull(message = ValidationError.UNAVAILABLE_FROM_DATE_EMPTY)
    private LocalDate unavailableFromDate;

    @NotNull(message = ValidationError.UNAVAILABLE_TO_DATE_EMPTY)
    private LocalDate unavailableToDate;

    @JsonProperty("unavailabilityType")
    private String unavailabilityType;
}
