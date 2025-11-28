package uk.gov.hmcts.reform.hmc.domain.model.enums;

import lombok.Getter;

@Getter
public enum ManageRequestStatus {

    SUCCESSFUL("successful"),
    FAILURE("failure");

    public final String label;

    ManageRequestStatus(String label) {
        this.label = label;
    }

}
