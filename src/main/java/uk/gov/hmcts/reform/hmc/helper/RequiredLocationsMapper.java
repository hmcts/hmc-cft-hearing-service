package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.LocationId;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequiredLocationsMapper {

    public List<RequiredLocationsEntity> modelToEntity(List<HearingLocation> hearingLocations,
                                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        List<RequiredLocationsEntity> requiredLocationsEntities = new ArrayList<>();
        for (HearingLocation location : hearingLocations) {
            final RequiredLocationsEntity locationEntity = new RequiredLocationsEntity();
            locationEntity.setLocationLevelType(location.getLocationType());
            locationEntity.setLocationId(LocationId.getByLabel(location.getLocationId()));
            locationEntity.setCaseHearing(caseHearingRequestEntity);
            requiredLocationsEntities.add(locationEntity);
        }
        return requiredLocationsEntities;
    }

}
