package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ListingReasonCode {
    NO_MAPPING_AVAILABLE("no-mapping-available"),
    USER_ADDED_COMMENTS("user-added-comments");

    private final String label;

    @Override
    public String toString() {
        return label;
    }
}
