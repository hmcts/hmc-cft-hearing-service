package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum CaseCategoryType {
    CASETYPE("caseType"),
    CASESUBTYPE("caseSubType");

    public final String label;

    CaseCategoryType(String label) {
        this.label = label;
    }
}
