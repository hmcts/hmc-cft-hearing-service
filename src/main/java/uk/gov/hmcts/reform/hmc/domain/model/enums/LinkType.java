package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;
import java.util.Locale;

public enum LinkType {
    ORDERED("Ordered"),
    SAME_SLOT("Same Slot");

    public final String label;

    LinkType(String label) {
        this.label = label;
    }

    public static LinkType getByLabel(String value) {
        return Arrays.stream(LinkType.values())
            .filter(eachLinkType -> eachLinkType.label.toLowerCase(Locale.ROOT)
                .equals(value.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
