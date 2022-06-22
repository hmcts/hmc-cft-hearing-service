package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseHearingRequestEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() throws CloneNotSupportedException {
            CaseHearingRequestEntity caseHearingRequest1 = TestingUtil.caseHearingRequestEntityWithPartyOrgForClone();
            CaseHearingRequestEntity response = (CaseHearingRequestEntity) caseHearingRequest1.clone();
            assertEquals(response.getHearingParties().get(0).getPartyReference(), TestingUtil.caseHearingRequestEntityWithPartyOrg().getHearingParties().get(0).getPartyReference());
            assertEquals(response.getHearingParties().get(0).getPartyRoleType(), TestingUtil.caseHearingRequestEntityWithPartyOrg().getHearingParties().get(0).getPartyRoleType());
            assertEquals(response.getHearingParties().get(0).getPartyType(), TestingUtil.caseHearingRequestEntityWithPartyOrg().getHearingParties().get(0).getPartyType());
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


    private CaseHearingRequestEntity caseHearingRequest(int version) {
        CaseHearingRequestEntity caseHearingRequest = new CaseHearingRequestEntity();
        caseHearingRequest.setVersionNumber(version);
        return caseHearingRequest;
    }

}
