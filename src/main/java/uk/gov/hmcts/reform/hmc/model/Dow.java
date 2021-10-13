package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum Dow {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    public final String label;

    Dow(String label) {
        this.label = label;
    }
}
