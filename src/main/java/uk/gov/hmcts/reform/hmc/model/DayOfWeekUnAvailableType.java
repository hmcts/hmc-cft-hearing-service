package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum DayOfWeekUnAvailableType {
    AM("AM"),
    PM("PM"),
    ALL("ALL");

    public final String label;

    DayOfWeekUnAvailableType(String label) {
        this.label = label;
    }
}
