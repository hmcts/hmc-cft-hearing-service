package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
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
    public static final String CATEGORY_PARENT_ERROR = "A caseSubType should have a defined category parent.";
    public static final String CATEGORY_PARENT_ERROR2 = "A casesType should not have a defined category parent.";

    private final Predicate<CaseCategory> isCategoryParentNorNull = caseCategory ->
        caseCategory.getCategoryParent() != null && caseCategory.getCategoryParent() != "";

    public List<CaseClassification> getCaseClassifications(CaseDetails caseDetails) {

        val caseClassificationTypes = getCaseCategories(caseDetails, CaseCategoryType.CASETYPE);
        validateSize(caseClassificationTypes,  new BadRequestException(CASE_TYPE_ERROR));
        validateCategoriesOfCaseType(caseClassificationTypes);

        val caseTypeValues = caseClassificationTypes.stream()
            .map(element -> element.getCategoryValue())
            .collect(Collectors.toList());

        val caseClassificationSubTypes = getCaseCategories(caseDetails, CaseCategoryType.CASESUBTYPE);
        validateSize(caseClassificationSubTypes,  new BadRequestException(CASE_SUBTYPE_ERROR));

        return caseClassificationSubTypes.stream()
            .map(caseClassificationSubType ->
                     buildCaseClassification(
                         caseDetails.getHmctsServiceCode(),
                         caseClassificationSubType,
                         caseTypeValues
                     )
            ).collect(Collectors.toList());
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
        if (!(caseCategory.getCategoryParent() != null
            && caseClassificationTypes.contains(caseCategory.getCategoryParent()))) {

            throw new BadRequestException(CATEGORY_PARENT_ERROR);
        }
    }

    private void validateCategoriesOfCaseType(List<CaseCategory> caseCategories) {
        if (caseCategories.stream().filter(isCategoryParentNorNull).findAny().isPresent()) {
            throw new BadRequestException(CATEGORY_PARENT_ERROR2);
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

