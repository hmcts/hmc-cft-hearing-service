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

    public static boolean isValid(String label) {
        return Arrays.stream(values()).anyMatch(enumStatus -> enumStatus.name().equals(label));
    }

    public static LinkType getByLabel(String label) {
        return Arrays.stream(LinkType.values())
                .filter(eachLinkType -> eachLinkType.toString().toLowerCase(Locale.ROOT)
                        .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

}
