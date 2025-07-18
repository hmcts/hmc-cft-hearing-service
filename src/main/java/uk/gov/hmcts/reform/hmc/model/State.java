package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum State {

    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    ADJOURNED("ADJOURNED");

    public final String label;

    State(String label) {
        this.label = label;
    }

    public static State getByLabel(String label) {
        State partyType = Arrays.stream(State.values())
            .filter(eachPartyType -> eachPartyType.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return partyType;
    }
}
