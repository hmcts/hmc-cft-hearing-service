package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LinkType {
    ORDERED("Ordered"),
    SAME_SLOT("Same Slot");

    public final String label;

    LinkType(String label) {
        this.label = label;
    }

    public static LinkType getByLabel(String label) {
        return Arrays.stream(LinkType.values())
            .filter(eachLinkType -> eachLinkType.label.toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

    public static boolean isValidLabel(String label) {
        return Arrays.stream(values())
                .anyMatch(eachLinkType -> eachLinkType.label.toLowerCase(Locale.ROOT)
                                .equals(label.toLowerCase(Locale.ROOT)));
    }

    public static boolean isValidName(String name) {
        return Arrays.stream(values())
                .anyMatch(eachLinkType -> eachLinkType.name().equals(name));
    }

}
