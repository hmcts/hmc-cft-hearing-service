package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.panelAuthorisationRequirementsEntity;

class PanelAuthorisationRequirementsEntityTest {
    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            PanelAuthorisationRequirementsEntity entity = panelAuthorisationRequirementsEntity();
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            PanelAuthorisationRequirementsEntity response = new PanelAuthorisationRequirementsEntity(entity);
            assertEquals("AuthorisationType1", response.getAuthorisationType());
            assertEquals("AuthorisationSubType2", response.getAuthorisationSubType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }

}
