package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import javax.validation.Valid;

@Data
@NoArgsConstructor
public class HearingVenue {
    private String locationIdCaseHQ;
    private String locationName;
    private JsonNode locationRegion;
    private JsonNode locationCluster;
    @Valid
    private ArrayList<VenueLocationReference> locationReferences;
}
