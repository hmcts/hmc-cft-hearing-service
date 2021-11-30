package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

@Getter
public enum LocationId {
    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    public final String label;

    LocationId(String label) {
        this.label = label;
    }
}
