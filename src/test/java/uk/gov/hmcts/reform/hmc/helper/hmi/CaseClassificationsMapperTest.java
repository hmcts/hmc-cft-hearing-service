package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseClassificationsMapperTest {

    private static final String SERVICE_CODE = "AB1";
    private static final String CATEGORY_VALUE = "CategoryValue";
    private static final String CATEGORY_VALUE_TWO = "CategoryValueTwo";

    @Test
    void shouldMapWhenCaseCategoriesHasBothCaseCategoryTypes() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(SERVICE_CODE);
        CaseCategory caseCategory = new CaseCategory();
        caseCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseCategory.setCategoryValue(CATEGORY_VALUE);
        CaseCategory caseCategoryTwo = new CaseCategory();
        caseCategoryTwo.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseCategoryTwo.setCategoryValue(CATEGORY_VALUE_TWO);
        List<CaseCategory> caseCategories = Arrays.asList(caseCategory, caseCategoryTwo);
        caseDetails.setCaseCategories(caseCategories);
        CaseClassification caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_VALUE)
            .build();
        CaseClassification caseClassificationTwo = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationSubType(CATEGORY_VALUE_TWO)
            .build();
        List<CaseClassification> expectedCaseClassifications = Arrays.asList(caseClassification, caseClassificationTwo);
        CaseClassificationsMapper caseClassificationsMapper = new CaseClassificationsMapper();
        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);
        assertEquals(expectedCaseClassifications, actualCaseClassifications);
    }



}
