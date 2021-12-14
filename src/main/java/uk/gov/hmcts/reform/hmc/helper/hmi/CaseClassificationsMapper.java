package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseClassificationsMapper {

    public List<CaseClassification> getCaseClassifications(CaseDetails caseDetails) {
        List<CaseClassification> caseClassifications = new ArrayList<>();
        for (CaseCategory caseCategory : caseDetails.getCaseCategories()) {
            if (caseCategory.getCategoryType().equalsIgnoreCase("caseType")) {
                CaseClassification caseClassification = CaseClassification.builder()
                    .caseClassificationService(caseDetails.getHmctsServiceCode())
                    .caseClassificationType(caseCategory.getCategoryValue())
                    .build();
                caseClassifications.add(caseClassification);
            } else if (caseCategory.getCategoryType().equalsIgnoreCase("caseSubType")) {
                CaseClassification caseClassification = CaseClassification.builder()
                    .caseClassificationService(caseDetails.getHmctsServiceCode())
                    .caseClassificationSubType(caseCategory.getCategoryValue())
                    .build();
                caseClassifications.add(caseClassification);
            }
        }
        return caseClassifications;
    }
}
