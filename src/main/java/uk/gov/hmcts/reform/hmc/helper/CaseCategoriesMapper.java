package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseCategoriesMapper {

    public List<CaseCategoriesEntity> modelToEntity(List<CaseCategory> caseCategories,
                                                    CaseHearingRequestEntity caseHearingRequestEntity) {
        List<CaseCategoriesEntity> caseCategoriesEntities = new ArrayList<>();
        for (CaseCategory category : caseCategories) {
            final CaseCategoriesEntity categoryEntity = new CaseCategoriesEntity();
            categoryEntity.setCategoryType(CaseCategoryType.valueOf(category.getCategoryType()));
            categoryEntity.setCaseCategoryValue(category.getCategoryValue());
            categoryEntity.setCaseHearing(caseHearingRequestEntity);
            caseCategoriesEntities.add(categoryEntity);
        }
        return caseCategoriesEntities;
    }
}
