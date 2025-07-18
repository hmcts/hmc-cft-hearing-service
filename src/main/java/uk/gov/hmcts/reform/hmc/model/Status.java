package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum Status {

    SUCCESSFUL("successful"),
    FAILURE("failure");

    public final String label;

    Status(String label) {
        this.label = label;
    }

    public static Status getByLabel(String label) {
        Status partyType = Arrays.stream(Status.values())
            .filter(eachPartyType -> eachPartyType.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return partyType;
    }
}
