package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum CaseCategoryType {
    CASETYPE("caseType"),
    CASESUBTYPE("caseSubType");

    public final String label;

    CaseCategoryType(String label) {
        this.label = label;
    }

    public static CaseCategoryType getByLabel(String label) {
        CaseCategoryType category = Arrays.stream(CaseCategoryType.values())
            .filter(eachCategory -> eachCategory.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return category;
    }


}
