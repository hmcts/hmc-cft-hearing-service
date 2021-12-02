package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseCategoriesMapperTest {

    @Test
    void modelToEntityTest() {
        CaseCategoriesMapper caseCategoriesMapper = new CaseCategoriesMapper();
        List<CaseCategory> caseCategories = getListOfCaseCategories();
        CaseHearingRequestEntity caseHearingEntity = new CaseHearingRequestEntity();
        List<CaseCategoriesEntity> entities = caseCategoriesMapper.modelToEntity(caseCategories, caseHearingEntity);
        assertEquals(CaseCategoryType.CASETYPE, entities.get(0).getCategoryType());
        assertEquals(CaseCategoryType.CASESUBTYPE, entities.get(1).getCategoryType());
        assertEquals("PROBATE", entities.get(0).getCaseCategoryValue());
        assertEquals("PROBATE2", entities.get(1).getCaseCategoryValue());

    }

    private List<CaseCategory> getListOfCaseCategories() {
        CaseCategory category1 = new CaseCategory();
        category1.setCategoryType("caseType");
        category1.setCategoryValue("PROBATE");

        CaseCategory category2 = new CaseCategory();
        category2.setCategoryType("caseSubType");
        category2.setCategoryValue("PROBATE2");

        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category1);
        caseCategories.add(category2);
        return caseCategories;
    }
}
