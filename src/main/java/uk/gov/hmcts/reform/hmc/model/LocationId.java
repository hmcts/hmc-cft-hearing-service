package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LocationId {
    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    public final String label;

    LocationId(String label) {
        this.label = label;
    }

    public static LocationId getByLabel(String label) {
        LocationId location = Arrays.stream(LocationId.values())
            .filter(eachLocation -> eachLocation.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return location;
    }
}
