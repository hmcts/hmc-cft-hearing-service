package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import java.util.ArrayList;
import java.util.List;

@Component
public class PanelUserRequirementsMapper {

    public List<PanelUserRequirementsEntity> modelToEntity(List<PanelPreference> panelPreferences,
                                                           CaseHearingRequestEntity caseHearingRequestEntity) {
        List<PanelUserRequirementsEntity> panelUserRequirementsEntities = new ArrayList<>();
        for (PanelPreference panelPreference : panelPreferences) {
            final PanelUserRequirementsEntity panelUserRequirementsEntity = new PanelUserRequirementsEntity();
            panelUserRequirementsEntity.setJudicialUserId(panelPreference.getMemberID());
            panelUserRequirementsEntity.setUserType(panelPreference.getMemberType());
            panelUserRequirementsEntity.setRequirementType(
                RequirementType.valueOf(panelPreference.getRequirementType()));
            panelUserRequirementsEntity.setCaseHearing(caseHearingRequestEntity);
            panelUserRequirementsEntities.add(panelUserRequirementsEntity);
        }
        return panelUserRequirementsEntities;
    }
}
