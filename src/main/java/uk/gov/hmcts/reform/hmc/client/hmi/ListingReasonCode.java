package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ListingReasonCode {
    NO_MAPPING_AVAILABLE("no-mapping-available");

    private final String label;

    @Override
    public String toString() {
        return label;
    }
}
