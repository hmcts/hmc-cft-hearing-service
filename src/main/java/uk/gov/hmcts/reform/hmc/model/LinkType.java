package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LinkType {

    IND("Ordered"),
    ORG("Same Slot");

    public final String linkType;

    LinkType(String linkType) {
        this.linkType = linkType;
    }
}
