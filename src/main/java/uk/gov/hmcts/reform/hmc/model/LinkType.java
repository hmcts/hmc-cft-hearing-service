package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum LinkType {

    ORDERED("Ordered"),
    SAME_SLOT("Same Slot");

    public final String linkType;

    LinkType(String linkType) {
        this.linkType = linkType;
    }
}
