package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.panelRequirementsEntity;

class PanelRequirementsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            PanelRequirementsEntity entity = panelRequirementsEntity();
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            PanelRequirementsEntity response = new PanelRequirementsEntity(entity);
            assertEquals("RoleType1", response.getRoleType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
