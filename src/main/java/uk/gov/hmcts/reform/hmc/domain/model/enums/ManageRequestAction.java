package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

@Getter
public enum ManageRequestAction {

    ROLLBACK("rollback"),
    FINAL_STATE_TRANSITION("final_state_transition");

    public final String label;

    ManageRequestAction(String label) {
        this.label = label;
    }

}

