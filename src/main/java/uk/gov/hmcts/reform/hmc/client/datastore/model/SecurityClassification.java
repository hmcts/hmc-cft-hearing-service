package uk.gov.hmcts.reform.hmc.client.datastore.model;

import java.io.Serializable;

public enum SecurityClassification implements Serializable {
    PUBLIC(1), PRIVATE(2), RESTRICTED(3);

    private final int rank;

    SecurityClassification(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

}
