package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.panelSpecialismsEntity;

class PanelSpecialismsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            PanelSpecialismsEntity entity = panelSpecialismsEntity();
            entity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            PanelSpecialismsEntity response = new PanelSpecialismsEntity(entity);
            assertEquals("Specialism 1", response.getSpecialismType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
