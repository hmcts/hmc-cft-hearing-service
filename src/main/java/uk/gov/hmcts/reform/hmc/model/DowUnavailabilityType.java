package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum DowUnavailabilityType {
    AM("AM"),
    PM("PM"),
    ALL("ALL");

    public final String label;

    DowUnavailabilityType(String label) {
        this.label = label;
    }
}
