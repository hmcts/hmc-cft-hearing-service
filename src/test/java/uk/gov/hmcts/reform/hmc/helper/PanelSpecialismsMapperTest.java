package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelSpecialismsMapperTest {

    @Test
    void modelToEntity() {
        PanelSpecialismsMapper mapper = new PanelSpecialismsMapper();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<String> panelSpecialisms = getPanelSpecialisms();
        List<PanelSpecialismsEntity> entity = mapper.modelToEntity(panelSpecialisms, caseHearingRequestEntity);
        assertEquals("Specialism 1", entity.get(0).getSpecialismType());
        assertEquals("Specialism 2", entity.get(1).getSpecialismType());
    }

    private List<String> getPanelSpecialisms() {
        List<String> panelSpecialisms = new ArrayList<>();
        panelSpecialisms.add("Specialism 1");
        panelSpecialisms.add("Specialism 2");
        return panelSpecialisms;
    }
}
