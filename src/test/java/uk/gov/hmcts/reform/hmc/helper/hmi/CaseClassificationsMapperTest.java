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
    private static final String CATEGORY_TYPE_SUBVALUE = "CategoryValue";


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
        caseCategoryTwo.setCategoryParent(CATEGORY_TYPE_VALUE);

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

    @Test //Success Scenario 1:
    void shouldMapForOneCaseSubTypeCategoriesWithSameParent() {

        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 2);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE);

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseSubTypeCategory
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        //Expected
        val caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE + 2)
            .build();

        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);

        assertEquals(Arrays.asList(caseClassification), actualCaseClassifications);
    }

    @Test //Success Scenario 2:
    void shouldMapForTwoCaseSubTypeCategoriesWithDifferentParent() {

        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 2);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE);

        val caseTypeCategory2 = new CaseCategory();
        caseTypeCategory2.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory2.setCategoryValue(CATEGORY_TYPE_VALUE + 2);

        val caseSubTypeCategory2 = new CaseCategory();
        caseSubTypeCategory2.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory2.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 4);
        caseSubTypeCategory2.setCategoryParent(CATEGORY_TYPE_VALUE + 2);

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseTypeCategory2,
            caseSubTypeCategory,
            caseSubTypeCategory2
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        //Expected
        val caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE + 2)
            .build();

        val caseClassification1 = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE + 2)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE + 4)
            .build();

        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);

        assertEquals(Arrays.asList(caseClassification, caseClassification1), actualCaseClassifications);
    }

    @Test //Success Scenario 3:
    void shouldMapForTwoCaseSubTypeCategoriesWithSameParent() {

        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 2);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory2 = new CaseCategory();
        caseSubTypeCategory2.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory2.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 4);
        caseSubTypeCategory2.setCategoryParent(CATEGORY_TYPE_VALUE);

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseSubTypeCategory,
            caseSubTypeCategory2
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        //Expected
        val caseClassification = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE + 2)
            .build();

        val caseClassification1 = CaseClassification.builder()
            .caseClassificationService(SERVICE_CODE)
            .caseClassificationType(CATEGORY_TYPE_VALUE)
            .caseClassificationSubType(CATEGORY_TYPE_SUBVALUE + 4)
            .build();

        List<CaseClassification> actualCaseClassifications = caseClassificationsMapper
            .getCaseClassifications(caseDetails);

        assertEquals(Arrays.asList(caseClassification, caseClassification1), actualCaseClassifications);
    }


    // ERRORS

    @Test // Failure Scenario 1 - CategoryParent does not match with categoryValue of the caseType:
    void shouldThrowsAnErrorDueToCaseTypeDoesNotHaveSubCase() {
        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 2);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE + "NO_MATCH");

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseSubTypeCategory
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        try {
            caseClassificationsMapper.getCaseClassifications(caseDetails);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(CaseClassificationsMapper.CASE_TYPE_CASE_SUBTYPE_ERROR));
        }
    }


    @Test //Failure Scenario 2 - CategoryParent is supplied for a categoryType of CaseType:
    void shouldThrowsAnErrorDueToCategoryParentIsSuppliedForCaseType() {
        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);
        caseTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE + "NO_MATCH");

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE + 2);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE + "NO_MATCH");

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseSubTypeCategory
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        try {
            caseClassificationsMapper.getCaseClassifications(caseDetails);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(CaseClassificationsMapper.CATEGORY_PARENT_NOT_EXPECTED_ERROR));
        }
    }

    @Test // Failure Scenario 4 - multiple case-types but one caseType without subtype
    void shouldThrowsAnErrorDueToMultipleCaseTypeDoesNotHaveSubCase() {
        val caseTypeCategory = new CaseCategory();
        caseTypeCategory.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory.setCategoryValue(CATEGORY_TYPE_VALUE);

        val caseSubTypeCategory = new CaseCategory();
        caseSubTypeCategory.setCategoryType(CaseCategoryType.CASESUBTYPE.getLabel().toUpperCase());
        caseSubTypeCategory.setCategoryValue(CATEGORY_TYPE_SUBVALUE);
        caseSubTypeCategory.setCategoryParent(CATEGORY_TYPE_VALUE);

        val caseTypeCategory1 = new CaseCategory();
        caseTypeCategory1.setCategoryType(CaseCategoryType.CASETYPE.getLabel().toLowerCase());
        caseTypeCategory1.setCategoryValue(CATEGORY_TYPE_VALUE + "NO_MATCH");

        List<CaseCategory> caseCategories = Arrays.asList(
            caseTypeCategory,
            caseSubTypeCategory,
            caseTypeCategory1
        );
        val caseDetails = createCaseDetails();
        caseDetails.setCaseCategories(caseCategories);

        try {
            caseClassificationsMapper.getCaseClassifications(caseDetails);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(CaseClassificationsMapper.CASE_TYPE_CASE_SUBTYPE_ERROR));
        }
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
