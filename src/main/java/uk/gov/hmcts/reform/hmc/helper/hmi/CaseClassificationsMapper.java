package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseClassificationsMapper {

    public List<CaseClassification> getCaseClassifications(CaseDetails caseDetails) {
        List<CaseClassification> caseClassifications = new ArrayList<>();
        for (CaseCategory caseCategory : caseDetails.getCaseCategories()) {
            CaseClassification.CaseClassificationBuilder caseClassificationBuilder = CaseClassification.builder()
                .caseClassificationService(caseDetails.getHmctsServiceCode());
            if (caseCategory.getCategoryType().equalsIgnoreCase(CaseCategoryType.CASETYPE.getLabel())) {
                caseClassificationBuilder.caseClassificationType(caseCategory.getCategoryValue());
            } else if (caseCategory.getCategoryType().equalsIgnoreCase(CaseCategoryType.CASESUBTYPE.getLabel())) {
                caseClassificationBuilder.caseClassificationSubType(caseCategory.getCategoryValue());
            }
            caseClassifications.add(caseClassificationBuilder.build());
        }
        return caseClassifications;
    }
}
