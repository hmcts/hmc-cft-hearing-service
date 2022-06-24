package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseCategoriesEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            CaseCategoriesEntity categoriesEntity = TestingUtil.caseCategoriesEntities().get(0);
            categoriesEntity.setCaseCategoryParent("A001");
            categoriesEntity.setCaseHearing(TestingUtil.getCaseHearingsEntities());
            CaseCategoriesEntity response = new CaseCategoriesEntity(categoriesEntity);
            assertEquals(CaseCategoryType.CASETYPE, response.getCategoryType());
            assertEquals("PROBATE", response.getCaseCategoryValue());
            assertEquals("A001", response.getCaseCategoryParent());
            assertEquals("ABA1", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
        }
    }
}
