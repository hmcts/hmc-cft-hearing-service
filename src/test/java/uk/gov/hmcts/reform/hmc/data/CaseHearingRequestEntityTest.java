package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseHearingRequestEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            CaseHearingRequestEntity caseHearingRequest1 = TestingUtil.caseHearingRequestEntityWithPartyOrgForClone();
            CaseHearingRequestEntity response = new CaseHearingRequestEntity(caseHearingRequest1);
            assertEquals(response.getHearingParties().get(0).getPartyReference(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg()
                             .getHearingParties().get(0).getPartyReference());
            assertEquals(response.getHearingParties().get(0).getPartyRoleType(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg()
                             .getHearingParties().get(0).getPartyRoleType());
            assertEquals(response.getHearingParties().get(0).getPartyType(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg().getHearingParties().get(0).getPartyType());
            assertEquals(1, response.getCaseCategories().size());
            assertEquals(1, response.getHearingParties().size());
            assertEquals(1, response.getNonStandardDurations().size());
            assertEquals(1, response.getRequiredFacilities().size());
            assertEquals(1, response.getRequiredLocations().size());
            assertEquals(1, response.getPanelAuthorisationRequirements().size());
            assertEquals(1, response.getPanelRequirements().size());
            assertEquals(1, response.getPanelUserRequirements().size());
        }
    }

}
