package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.CaseClassification;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class CaseClassificationsMapper {

    public static final String CASE_TYPE_ERROR = "caseCategories element should have a caseType.";
    public static final String CASE_SUBTYPE_ERROR = "caseCategories element should have a caseSubType.";
    public static final String CATEGORY_PARENT_EXPECTED_ERROR = "A caseSubType should have a defined category parent.";
    public static final String CATEGORY_PARENT_NOT_EXPECTED_ERROR =
        "A caseType should not have a defined category parent.";
    public static final String CASE_TYPE_CASE_SUBTYPE_ERROR = "A caseType should have a defined caseSubType.";

    private final Predicate<CaseCategory> isCategoryParentNotNull = caseCategory ->
        !StringUtils.isEmpty(caseCategory.getCategoryParent());

    public List<CaseClassification> getCaseClassifications(CaseDetails caseDetails) {

        val caseClassificationTypes = getCaseCategories(caseDetails, CaseCategoryType.CASETYPE);
        validateSize(caseClassificationTypes,  new BadRequestException(CASE_TYPE_ERROR));
        validateCategoriesOfCaseType(caseClassificationTypes);

        val caseTypeValues = caseClassificationTypes.stream()
            .map(element -> element.getCategoryValue())
            .collect(Collectors.toList());

        val caseClassificationSubTypes = getCaseCategories(caseDetails, CaseCategoryType.CASESUBTYPE);
        validateSize(caseClassificationSubTypes,  new BadRequestException(CASE_SUBTYPE_ERROR));
        validateCaseTypeAndCaseSubType(caseTypeValues, caseClassificationSubTypes);

        return caseClassificationSubTypes.stream()
            .map(caseClassificationSubType ->
                     buildCaseClassification(
                         caseDetails.getHmctsServiceCode(),
                         caseClassificationSubType,
                         caseTypeValues
                     )
            ).collect(Collectors.toList());
    }

    private void validateCaseTypeAndCaseSubType(List<String> caseTypeValues,
                                                List<CaseCategory> caseClassificationSubTypes) {

        val categoryParents = caseClassificationSubTypes.stream()
            .map(CaseCategory::getCategoryParent)
            .collect(Collectors.toList());

        caseTypeValues.stream()
            .forEach(caseType -> {
                if (!categoryParents.contains(caseType)) {
                    throw new BadRequestException(CASE_TYPE_CASE_SUBTYPE_ERROR);
                }
            });
    }

    private CaseClassification buildCaseClassification(String hmctsServiceCode, CaseCategory caseClassificationSubType,
                                                       List<String> caseClassificationTypes) {

        validateCategoryParent(caseClassificationSubType, caseClassificationTypes);

        return CaseClassification.builder()
            .caseClassificationService(hmctsServiceCode)
            .caseClassificationType(caseClassificationSubType.getCategoryParent())
            .caseClassificationSubType(caseClassificationSubType.getCategoryValue())
            .build();

    }

    private void validateCategoryParent(CaseCategory caseCategory, List<String> caseClassificationTypes) {
        if (caseCategory.getCategoryParent() == null
            || !caseClassificationTypes.contains(caseCategory.getCategoryParent())) {

            throw new BadRequestException(CATEGORY_PARENT_EXPECTED_ERROR);
        }
    }

    private void validateCategoriesOfCaseType(List<CaseCategory> caseCategories) {
        if (caseCategories.stream().anyMatch(isCategoryParentNotNull)) {
            throw new BadRequestException(CATEGORY_PARENT_NOT_EXPECTED_ERROR);
        }
    }

    private void validateSize(List<CaseCategory> caseCategories, BadRequestException badRequestException) {
        if (caseCategories.isEmpty()) {
            throw badRequestException;
        }
    }

    private List<CaseCategory> getCaseCategories(CaseDetails caseDetails, CaseCategoryType caseSubtype) {
        return caseDetails.getCaseCategories().stream()
            .filter(element -> element.getCategoryType().equalsIgnoreCase(caseSubtype.getLabel()))
            .collect(Collectors.toList());
    }
}

