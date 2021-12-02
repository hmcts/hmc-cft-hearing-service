package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum RequirementType {
    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    public final String label;

    RequirementType(String label) {
        this.label = label;
    }

    public static RequirementType getByLabel(String label) {
        RequirementType requirementType = Arrays.stream(RequirementType.values())
            .filter(eachRequirement -> eachRequirement.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return requirementType;
    }
}
