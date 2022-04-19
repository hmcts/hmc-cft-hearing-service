package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseClassificationsMapper {

    public static final String CASE_TYPE_ERROR = "caseCategories element should have a caseType.";
    public static final String CASE_SUBTYPE_ERROR = "caseCategories element should have a caseSubType.";

    public List<CaseClassification> getCaseClassifications(CaseDetails caseDetails) {
        List<CaseClassification> caseClassifications = new ArrayList<>();

        val caseClassificationType = caseDetails.getCaseCategories().stream().filter(
            element -> element.getCategoryType().equalsIgnoreCase(CaseCategoryType.CASETYPE.getLabel())
        ).findFirst().orElseThrow(() -> new BadRequestException(CASE_TYPE_ERROR));

        val caseClassificationSubType = caseDetails.getCaseCategories().stream().filter(
            element -> element.getCategoryType().equalsIgnoreCase(CaseCategoryType.CASESUBTYPE.getLabel())
        ).findFirst().orElseThrow(() -> new BadRequestException(CASE_SUBTYPE_ERROR));

        val caseClassificationBuilder = CaseClassification.builder()
            .caseClassificationService(caseDetails.getHmctsServiceCode());
        caseClassificationBuilder.caseClassificationType(caseClassificationType.getCategoryValue());
        caseClassificationBuilder.caseClassificationSubType(caseClassificationSubType.getCategoryValue());
        caseClassifications.add(caseClassificationBuilder.build());
        return caseClassifications;
    }
}
