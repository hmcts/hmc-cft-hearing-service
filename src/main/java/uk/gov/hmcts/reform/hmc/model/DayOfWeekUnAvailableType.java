package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum DayOfWeekUnAvailableType {
    AM("AM"),
    PM("PM"),
    ALL("ALL");

    public final String label;

    DayOfWeekUnAvailableType(String label) {
        this.label = label;
    }

    public static DayOfWeekUnAvailableType getByLabel(String label) {
        DayOfWeekUnAvailableType dowUnavailable = Arrays.stream(DayOfWeekUnAvailableType.values())
            .filter(eachDowUnavailable -> eachDowUnavailable.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return dowUnavailable;
    }

}
