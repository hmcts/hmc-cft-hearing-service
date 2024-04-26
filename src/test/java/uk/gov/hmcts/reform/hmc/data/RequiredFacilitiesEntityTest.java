package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.facilityEntity;

class RequiredFacilitiesEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            RequiredFacilitiesEntity entity = facilityEntity();
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            RequiredFacilitiesEntity response = new RequiredFacilitiesEntity(entity);
            assertEquals("RoleType1", response.getFacilityType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
