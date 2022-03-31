package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HearingVenue {
    private String locationIdCaseHQ;
    private String locationName;
    private String locationRegion;
    private String locationCluster;
    private VenueLocationReference locationReference;
}
