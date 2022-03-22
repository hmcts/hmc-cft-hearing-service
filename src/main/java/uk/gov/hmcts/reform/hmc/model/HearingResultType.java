package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum HearingResultType {
    COMPLETED("COMPLETED"),
    ADJOURNED("ADJOURNED"),
    CANCELLED("CANCELLED");

    public final String label;

    HearingResultType(String label) {
        this.label = label;
    }

    public static HearingResultType getByLabel(String label) {
        return Arrays.stream(HearingResultType.values())
            .filter(eachResultType -> eachResultType.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

}
