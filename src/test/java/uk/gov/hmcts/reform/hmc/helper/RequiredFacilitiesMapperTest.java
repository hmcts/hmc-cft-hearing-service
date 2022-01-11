package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredFacilitiesMapperTest {

    @Test
    void modelToEntityTest() {
        RequiredFacilitiesMapper mapper = new RequiredFacilitiesMapper();
        List<String> facilities = getRequiredFacilities();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<RequiredFacilitiesEntity> entities = mapper.modelToEntity(facilities, caseHearingRequestEntity);
        assertEquals("Facility1", entities.get(0).getFacilityType());
        assertEquals("Facility2", entities.get(1).getFacilityType());
    }

    private List<String> getRequiredFacilities() {
        List<String> facilities = new ArrayList<>();
        facilities.add("Facility1");
        facilities.add("Facility2");
        return facilities;
    }
}
