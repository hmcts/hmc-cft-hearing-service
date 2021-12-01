package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.RequirementType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelUserRequirementsMapperTest {

    @Test
    void modelToEntity() {
        PanelUserRequirementsMapper mapper = new PanelUserRequirementsMapper();
        List<PanelPreference> panelPreferences = getPanelPreferences();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<PanelUserRequirementsEntity> entities = mapper.modelToEntity(panelPreferences, caseHearingRequestEntity);
        assertEquals("MID123", entities.get(0).getJudicialUserId());
        assertEquals("Member type 1", entities.get(0).getUserType());
        assertEquals(RequirementType.MUSTINC, entities.get(0).getRequirementType());
        assertEquals("MID999", entities.get(1).getJudicialUserId());
        assertEquals("Member type 2", entities.get(1).getUserType());
        assertEquals(RequirementType.OPTINC, entities.get(1).getRequirementType());

    }

    private List<PanelPreference> getPanelPreferences() {

        PanelPreference preference1 = new PanelPreference();
        preference1.setMemberID("MID123");
        preference1.setMemberType("Member type 1");
        preference1.setRequirementType("Mustinc");

        PanelPreference preference2 = new PanelPreference();
        preference2.setMemberID("MID999");
        preference2.setMemberType("Member type 2");
        preference2.setRequirementType("OPTINC");

        List<PanelPreference> preferences = new ArrayList<>();
        preferences.add(preference1);
        preferences.add(preference2);
        return preferences;

    }
}
