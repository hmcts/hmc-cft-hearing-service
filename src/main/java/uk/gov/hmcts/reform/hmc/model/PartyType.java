package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum PartyType {

    IND("IND"),
    ORG("ORG");

    public final String label;

    PartyType(String label) {
        this.label = label;
    }

    public static PartyType getByLabel(String label) {
        PartyType partyType = Arrays.stream(PartyType.values())
            .filter(eachPartyType -> eachPartyType.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return partyType;
    }
}
