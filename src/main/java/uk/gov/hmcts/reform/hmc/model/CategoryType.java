package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum CategoryType {
    CASETYPE("caseType"),
    CASESUBTYPE("caseSubType");

    public final String label;

    CategoryType(String label) {
        this.label = label;
    }
}
