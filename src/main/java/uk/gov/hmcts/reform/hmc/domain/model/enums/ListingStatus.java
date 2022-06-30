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

    public static ListingStatus getByName(String name) {
        return Arrays.stream(ListingStatus.values())
                .filter(eachStatus -> eachStatus.name().toLowerCase(Locale.ROOT)
                        .equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

    public static boolean isValidLabel(String label) {
        return Arrays.stream(values())
                .anyMatch(eachStatus -> eachStatus.label.toLowerCase(Locale.ROOT)
                                .equals(label.toLowerCase(Locale.ROOT)));
    }

    public static boolean isValidName(String name) {
        return Arrays.stream(values())
                .anyMatch(eachStatus -> eachStatus.name().equals(name));
    }

    public static String getLabel(String name) {
        ListingStatus status =  getByName(name);
        return null == status ? null : status.label;
    }

}
