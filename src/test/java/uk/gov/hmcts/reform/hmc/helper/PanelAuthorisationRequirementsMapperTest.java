package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PanelAuthorisationRequirementsMapperTest {

    @Test
    void modelToEntity() {
        PanelRequirements panelRequirements = TestingUtil.panelRequirements();
        CaseHearingRequestEntity entity = new CaseHearingRequestEntity();
        PanelAuthorisationRequirementsMapper mapper = new PanelAuthorisationRequirementsMapper();
        List<PanelAuthorisationRequirementsEntity> entities = mapper.modelToEntity(panelRequirements, entity);
        assertEquals("AuthorisationType1", entities.get(0).getAuthorisationType());
        assertNull(entities.get(0).getAuthorisationSubType());
        assertEquals("AuthorisationSubType2", entities.get(1).getAuthorisationSubType());
        assertNull(entities.get(1).getAuthorisationType());
    }
}

