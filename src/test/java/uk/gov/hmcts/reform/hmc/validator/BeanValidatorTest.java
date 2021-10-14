package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeanValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeEach
    void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void shouldHaveHearingDetailsViolations() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(null);
        hearingDetails.setHearingType(
            "HearingType more than 40HearingType more than 40HearingType more than 40HearingType more than 40");
        hearingDetails.setDuration(-1);
        hearingDetails.setHearingPriorityType("");
        hearingDetails.setNumberOfPhysicalAttendees(-1);
        hearingDetails.setLeadJudgeContractType("Lead judge contractLead judge contractLead judge contractLead judge "
                                                    + "contractLead judge contractLead judge contract");
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(8, violations.size());
        assertTrue(validationErrors.contains(ValidationError.AUTO_LIST_FLAG_NULL_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.HEARING_TYPE_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.HEARING_WINDOW_NULL));
        assertTrue(validationErrors.contains(ValidationError.DURATION_MIN_VALUE));
        assertTrue(validationErrors.contains(ValidationError.HEARING_PRIORITY_TYPE));
        assertTrue(validationErrors.contains(ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE));
        assertTrue(validationErrors.contains(ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH));
    }

    @Test
    void shouldHaveHearingDetails_NonStandardHearingDurationReasonsViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        String reason = "NonStandardHearingDurationReasonsnonStandardHearingDurationReasonsnonStandardHearingDura ";
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList(reason));
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG));
    }

    @Test
    void shouldHaveHearingDetails_HearingLocationRequiredViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        hearingDetails.setHearingLocations(null);
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.HEARING_LOCATION_EMPTY));
    }

    @Test
    void shouldHaveHearingDetails_HearingLocationDetailsViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationId("");
        hearingLocation.setLocationId("cluster");
        HearingLocation[] hearingLocations = {hearingLocation};
        hearingDetails.setHearingLocations(hearingLocations);
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.LOCATION_TYPE_EMPTY));
    }

    @Test
    void shouldHave_NoHearingDetailsViolation() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
        assertEquals(0, violations.size());
    }

    @Test
    void shouldHave_NoCaseDetailsViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
        assertEquals(0, violations.size());
    }


    @Test
    void shouldHave_CaseDetailsViolations() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("");
        caseDetails.setCaseRef("");
        caseDetails.setRequestTimeStamp("");
        caseDetails.setExternalCaseReference("externalCaseReferenceexternalCaseReferenceexternalCaseReferenceexternal"
                                                 + "CaseReference");
        caseDetails.setCaseDeepLink("abc");
        caseDetails.setHmctsInternalCaseName("");
        caseDetails.setPublicCaseName("");
        caseDetails.setCaseCategories(new CaseCategory[0]);
        caseDetails.setCaseManagementLocationCode("");
        caseDetails.setCaseRestrictedFlag(null);
        caseDetails.setCaseSlaStartDate("");
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(12, violations.size());
        assertTrue(validationErrors.contains(ValidationError.HMCTS_SERVICE_CODE_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.CASE_REF_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.REQUEST_TIMESTAMP_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.CASE_DEEP_LINK_INVALID));
        assertTrue(validationErrors.contains(ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.PUBLIC_CASE_NAME_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY));

    }

    @Test
    void shouldHave_CaseCategoryViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("externalCaseReferenceexternalCaseReferenceexternalCaseReferenceexternal"
                                      + "CaseReference");
        CaseCategory[] caseCategories = {category};
        caseDetails.setCaseCategories(caseCategories);
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.CATEGORY_VALUE));
    }

    @Test
    void shouldHave_NoRequestDetailsViolations() {
        RequestDetails requestDetails = TestingUtil.requestDetails();
        Set<ConstraintViolation<RequestDetails>> violations = validator.validate(requestDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
        assertEquals(0, violations.size());

    }

    @Test
    void shouldHave_RequestDetailsViolations() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp(null);
        Set<ConstraintViolation<RequestDetails>> violations = validator.validate(requestDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY));
    }


}
