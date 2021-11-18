package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequiredFacilitiesMapper {

    public RequiredFacilitiesMapper() {
    }

    public List<RequiredFacilitiesEntity> modelToEntity(HearingRequest hearingRequest,
                                                        CaseHearingRequestEntity caseHearingRequestEntity) {
        List<RequiredFacilitiesEntity> requiredFacilitiesEntities = new ArrayList<>();
        List<String> facilities = hearingRequest.getHearingDetails().getFacilitiesRequired();
        for (String facility : facilities) {
            final RequiredFacilitiesEntity requiredFacilitiesEntity = new RequiredFacilitiesEntity();
            requiredFacilitiesEntity.setFacilityType(facility);
            requiredFacilitiesEntity.setCaseHearing(caseHearingRequestEntity);
            requiredFacilitiesEntities.add(requiredFacilitiesEntity);
        }
        return requiredFacilitiesEntities;
    }
}
