package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@NoArgsConstructor
public class HearingVenue {
    private String locationIdCaseHQ;
    private String locationName;
    private String locationRegion;
    private String locationCluster;
    @Valid
    private VenueLocationReference locationReference;
}
