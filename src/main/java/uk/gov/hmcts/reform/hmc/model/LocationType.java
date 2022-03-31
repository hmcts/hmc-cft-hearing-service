package uk.gov.hmcts.reform.hmc.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LocationType {
    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    public final String label;

    LocationType(String label) {
        this.label = label;
    }

    public static LocationType getByLabel(String label) {
        LocationType location = Arrays.stream(LocationType.values())
            .filter(eachLocation -> eachLocation.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
        return location;
    }
}
