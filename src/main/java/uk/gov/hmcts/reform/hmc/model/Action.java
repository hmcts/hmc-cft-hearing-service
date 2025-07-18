package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum Action {

    ROLL_BACK("rollback"),
    FINAL_STATE_TRANSITION("final_state_transition");

    public final String label;

    Action(String label) {
        this.label = label;
    }

    public static Action getByLabel(String label) {
        Action partyType = Arrays.stream(Action.values())
            .filter(eachPartyType -> eachPartyType.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return partyType;
    }
}
