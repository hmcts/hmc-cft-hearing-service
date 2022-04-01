package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum DayOfWeekUnAvailableType {
    AM("AM"),
    PM("PM"),
    ALL("All Day");

    public final String label;

    DayOfWeekUnAvailableType(String label) {
        this.label = label;
    }

    public static DayOfWeekUnAvailableType getByLabel(String label) {
        if (label == null) {
            throw new BadRequestException("unsupported type for unavailability type");
        }
        DayOfWeekUnAvailableType dowUnavailable = Arrays.stream(DayOfWeekUnAvailableType.values())
            .filter(eachDowUnavailable -> eachDowUnavailable.getLabel().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny()
            .orElseThrow(() -> new BadRequestException("unsupported type for unavailability type"));
        return dowUnavailable;
    }

}
