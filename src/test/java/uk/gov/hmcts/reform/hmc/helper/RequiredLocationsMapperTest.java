package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.LocationType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredLocationsMapperTest {

    @Test
    void modelToEntityTest() {
        RequiredLocationsMapper mapper = new RequiredLocationsMapper();
        List<HearingLocation> locations = getRequiredLocations();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<RequiredLocationsEntity> entities = mapper.modelToEntity(locations, caseHearingRequestEntity);
        assertEquals(LocationType.COURT, entities.get(0).getLocationType());
        assertEquals("Location type", entities.get(0).getLocationLevelType());
        assertEquals(LocationType.CLUSTER, entities.get(1).getLocationType());
        assertEquals("Location type2", entities.get(1).getLocationLevelType());
    }

    private List<HearingLocation> getRequiredLocations() {
        HearingLocation location1 = new HearingLocation();
        location1.setLocationType("COURT");
        location1.setLocationId("Location id");

        HearingLocation location2 = new HearingLocation();
        location2.setLocationType("CLUSTER");
        location2.setLocationId("Location id2");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingLocations.add(location2);
        return hearingLocations;
    }
}
