package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum PartyType {

    IND("IND"),
    ORG("ORG");

    public final String label;

    PartyType(String label) {
        this.label = label;
    }
}
