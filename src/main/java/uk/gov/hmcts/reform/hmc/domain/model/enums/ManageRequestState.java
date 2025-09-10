package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

@Getter
public enum ManageRequestState {

    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    ADJOURNED("ADJOURNED");

    public final String label;

    ManageRequestState(String label) {
        this.label = label;
    }

}
