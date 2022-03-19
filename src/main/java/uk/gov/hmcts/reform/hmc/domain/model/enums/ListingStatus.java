package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;
import java.util.Locale;

public enum ListingStatus {
    DRAFT("Draft"),
    FIXED("Fixed"),
    PROVISIONAL("Provisional");

    public final String label;

    ListingStatus(String label) {
        this.label = label;
    }

    public static ListingStatus getByLabel(String label) {
        return Arrays.stream(ListingStatus.values())
                .filter(eachStatus -> eachStatus.toString().toLowerCase(Locale.ROOT)
                        .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

}
