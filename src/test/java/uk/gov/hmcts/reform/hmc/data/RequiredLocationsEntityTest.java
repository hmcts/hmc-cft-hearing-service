package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.LocationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.locationEntity;

class RequiredLocationsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            RequiredLocationsEntity entity = locationEntity();
            entity.setLocationId("Location1");
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            RequiredLocationsEntity response = new RequiredLocationsEntity(entity);
            assertEquals(LocationType.CLUSTER, response.getLocationLevelType());
            assertEquals("Location1", response.getLocationId());
            assertEquals("ABA1", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
