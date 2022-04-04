package uk.gov.hmcts.reform.hmc.model.hmi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import java.time.LocalDate;

@Data
@Builder
public class EntityUnavailableDate {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T00:00:00Z'")
    private LocalDate unavailableStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T00:00:00Z'")
    private LocalDate unavailableEndDate;

    @EnumPattern(enumClass = DayOfWeekUnAvailableType.class, fieldName = "dowUnavailabilityType")
    private String unavailableType;

}
