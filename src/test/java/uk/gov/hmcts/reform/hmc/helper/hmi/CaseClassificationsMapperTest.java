package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseClassificationsMapperTest {

    private static final String SERVICE_CODE = "AB1";
    private static final String CATEGORY_TYPE_VALUE = "CategoryValue";
    private static final String CATEGORY_TYPE_SUBVALUE = "CategoryValueTwo";


    private CaseClassificationsMapper caseClassificationsMapper = new CaseClassificationsMapper();

    private CaseDetails createCaseDetails() {
        val caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(SERVICE_CODE);
        return caseDetails;
    }

    @Test
    void shouldMapWhenCaseCategoriesHasBothCaseCategoryTypes() {

        val caseCategory = new CaseCategory();
        caseCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseCategoryTwo = new CaseCategory();
        caseCategoryTwo.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseCategoryTwo.setCategoryValue(CATEGORY_TYPE_SUBVALUE);
        List<CaseCategory> caseCategories = Arrays.asList(caseCategory, caseCategoryTwo);
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);
        //Expected
        val caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE)
            .build();

        List<CaseClassification> expectedCaseClassifications = Arrays.asList(caseClassification);

        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);
        assertEquals(expectedCaseClassifications, actualCaseClassifications);
    }

    @Test
    void shouldMapWhenCaseCategoriesHasTwoCaseCategories() {

        val caseCategory = new CaseCategory();
        caseCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseCategoryTwo = new CaseCategory();
        caseCategoryTwo.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseCategoryTwo.setCategoryValue(CATEGORY_TYPE_SUBVALUE);

        val caseCategoryThree = new CaseCategory();
        caseCategoryThree.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseCategoryThree.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseCategoryFour = new CaseCategory();
        caseCategoryFour.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseCategoryFour.setCategoryValue(CATEGORY_TYPE_SUBVALUE);

        List<CaseCategory> caseCategories = Arrays.asList(
            caseCategory,
            caseCategoryTwo,
            caseCategoryThree,
            caseCategoryFour
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        //Expected
        val caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE)
            .build();

        List<CaseClassification> expectedCaseClassifications = Arrays.asList(caseClassification);

        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);
        assertEquals(expectedCaseClassifications, actualCaseClassifications);
    }

    @Test
    void shouldThrowsAnErrorDueToMissingCategoryType() {
        val caseDetails = createCaseDetails();
        val caseCategoryTwo = new CaseCategory();
        caseCategoryTwo.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseCategoryTwo.setCategoryValue(CATEGORY_TYPE_SUBVALUE);
        List<CaseCategory> caseCategories = Arrays.asList(caseCategoryTwo);
        caseDetails.setCaseCategories(caseCategories);
        try {
            caseClassificationsMapper.getCaseClassifications(caseDetails);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(CaseClassificationsMapper.CASE_TYPE_ERROR));
        }
    }

    @Test
    void shouldThrowsAnErrorDueToMissingCategorySubType() {
        val caseDetails = createCaseDetails();
        val caseCategory = new CaseCategory();
        caseCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseCategory.setCategoryValue(CATEGORY_TYPE_VALUE);
        List<CaseCategory> caseCategories = Arrays.asList(caseCategory);
        caseDetails.setCaseCategories(caseCategories);
        try {
            caseClassificationsMapper.getCaseClassifications(caseDetails);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(CaseClassificationsMapper.CASE_SUBTYPE_ERROR));
        }
    }
}
