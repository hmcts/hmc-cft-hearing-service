package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum RequirementType {
    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    public final String label;

    RequirementType(String label) {
        this.label = label;
    }
}
