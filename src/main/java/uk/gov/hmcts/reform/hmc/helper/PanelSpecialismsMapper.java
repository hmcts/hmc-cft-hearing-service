package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class PanelSpecialismsMapper {

    public PanelSpecialismsMapper() {
    }

    public List<PanelSpecialismsEntity> modelToEntity(List<String> panelSpecialisms,
                                                      CaseHearingRequestEntity caseHearingRequestEntity) {
        List<PanelSpecialismsEntity> panelSpecialismEntities = new ArrayList<>();
        for (String panelSpecialism : panelSpecialisms) {
            final PanelSpecialismsEntity panelSpecialismsEntity = new PanelSpecialismsEntity();
            panelSpecialismsEntity.setSpecialismType(panelSpecialism);
            panelSpecialismsEntity.setCaseHearing(caseHearingRequestEntity);
            panelSpecialismEntities.add(panelSpecialismsEntity);
        }
        return panelSpecialismEntities;
    }
}
