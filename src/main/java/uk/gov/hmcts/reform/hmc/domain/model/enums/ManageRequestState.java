package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum ManageRequestState {

    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    ADJOURNED("ADJOURNED");

    public final String label;

    ManageRequestState(String label) {
        this.label = label;
    }

    public static ManageRequestState getByLabel(String label) {
        return Arrays.stream(ManageRequestState.values())
            .filter(eachState -> eachState.getLabel().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT)))
            .findAny()
            .orElse(null);
    }
}
