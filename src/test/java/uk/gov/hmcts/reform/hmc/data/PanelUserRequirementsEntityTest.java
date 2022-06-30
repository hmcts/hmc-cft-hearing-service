package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.panelUserRequirementsEntity;

class PanelUserRequirementsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            PanelUserRequirementsEntity entity = panelUserRequirementsEntity();
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            PanelUserRequirementsEntity response = new PanelUserRequirementsEntity(entity);
            assertEquals("Type 1", response.getUserType());
            assertEquals("judge1", response.getJudicialUserId());
            assertEquals(RequirementType.MUSTINC, response.getRequirementType());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
