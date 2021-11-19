package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelRequirementsMapperTest {

    @Test
    void modelToEntity() {
        PanelRequirementsMapper mapper = new PanelRequirementsMapper();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<String> roleTypes = getRoleTypes();
        List<PanelRequirementsEntity> entities = mapper.modelToEntity(roleTypes, caseHearingRequestEntity);
        assertEquals("RoleType1", entities.get(0).getRoleType());
        assertEquals("RoleType2", entities.get(1).getRoleType());
    }


    private List<String> getRoleTypes() {
        List<String> roleTypes = new ArrayList<>();
        roleTypes.add("RoleType1");
        roleTypes.add("RoleType2");
        return roleTypes;
    }
}
