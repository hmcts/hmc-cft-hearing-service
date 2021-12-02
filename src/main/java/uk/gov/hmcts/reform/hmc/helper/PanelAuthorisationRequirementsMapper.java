package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;

import java.util.ArrayList;
import java.util.List;

@Component
public class PanelAuthorisationRequirementsMapper {

    public List<PanelAuthorisationRequirementsEntity> modelToEntity(PanelRequirements panelRequirements,
                                                                    CaseHearingRequestEntity caseHearingRequestEntity) {
        List<PanelAuthorisationRequirementsEntity> panelAuthorisationRequirementsEntities = new ArrayList<>();
        if (null != panelRequirements.getAuthorisationTypes()) {
            setAuthorisationType(panelRequirements, caseHearingRequestEntity, panelAuthorisationRequirementsEntities);
        }
        if (null != panelRequirements.getAuthorisationSubType()) {
            setAuthorisationSubType(
                panelRequirements,
                caseHearingRequestEntity,
                panelAuthorisationRequirementsEntities
            );
        }
        return panelAuthorisationRequirementsEntities;
    }

    private void setAuthorisationSubType(PanelRequirements panelRequirements,
                                         CaseHearingRequestEntity caseHearingRequestEntity,
                                         List<PanelAuthorisationRequirementsEntity> authorisationEntities) {
        for (String authorisationSubType : panelRequirements.getAuthorisationSubType()) {
            final PanelAuthorisationRequirementsEntity entity = new PanelAuthorisationRequirementsEntity();
            entity.setAuthorisationSubType(authorisationSubType);
            entity.setCaseHearing(caseHearingRequestEntity);
            authorisationEntities.add(entity);
        }
    }

    private void setAuthorisationType(PanelRequirements panelRequirements,
                                      CaseHearingRequestEntity caseHearingRequestEntity,
                                      List<PanelAuthorisationRequirementsEntity> authorisationRequirementsEntities) {
        for (String authorisationType : panelRequirements.getAuthorisationTypes()) {
            final PanelAuthorisationRequirementsEntity entity = new PanelAuthorisationRequirementsEntity();
            entity.setAuthorisationType(authorisationType);
            entity.setCaseHearing(caseHearingRequestEntity);
            authorisationRequirementsEntities.add(entity);
        }
    }
}
