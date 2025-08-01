package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum ManageRequestAction {

    ROLLBACK("rollback"),
    FINAL_STATE_TRANSITION("final_state_transition");

    public final String label;

    ManageRequestAction(String label) {
        this.label = label;
    }

    public static ManageRequestAction getByLabel(String label) {
        return Arrays.stream(ManageRequestAction.values())
            .filter(eachAction -> eachAction.getLabel().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT)))
            .findAny()
            .orElse(null);
    }
}

