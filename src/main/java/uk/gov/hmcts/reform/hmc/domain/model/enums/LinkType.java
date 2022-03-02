package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;

public enum LinkType {
    ORDERED("Ordered"),
    SAME_SLOT("Same Slot");

    public final String linkType;

    LinkType(String linkType) {
        this.linkType = linkType;
    }

    public static boolean isValid(String linkType) {
        return Arrays.stream(values()).anyMatch(enumStatus -> enumStatus.name().equals(linkType));
    }
}
