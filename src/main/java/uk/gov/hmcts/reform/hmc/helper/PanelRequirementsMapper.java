package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class PanelRequirementsMapper {

    public List<PanelRequirementsEntity> modelToEntity(List<String> roleTypes,
                                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        List<PanelRequirementsEntity> panelRequirementsEntities = new ArrayList<>();
        for (String roleType : roleTypes) {
            final PanelRequirementsEntity panelRequirementsEntity = new PanelRequirementsEntity();
            panelRequirementsEntity.setRoleType(roleType);
            panelRequirementsEntity.setCaseHearing(caseHearingRequestEntity);
            panelRequirementsEntities.add(panelRequirementsEntity);
        }
        return panelRequirementsEntities;
    }
}
