package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
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
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_VALUE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_LOCATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_TYPE_EMPTY;


class BeanValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(BeanValidatorTest.class);

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
        assertEquals(10, violations.size());
        assertTrue(validationErrors.contains(ValidationError.AUTO_LIST_FLAG_NULL_EMPTY));
        assertTrue(validationErrors.contains(ValidationError.HEARING_TYPE_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.HEARING_WINDOW_NULL));
        assertTrue(validationErrors.contains(ValidationError.DURATION_MIN_VALUE));
        assertTrue(validationErrors.contains(ValidationError.HEARING_PRIORITY_TYPE));
        assertTrue(validationErrors.contains(ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE));
        assertTrue(validationErrors.contains(ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.HEARING_LOCATION_EMPTY));
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
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains(HEARING_LOCATION_EMPTY));
    }

    @Test
    void shouldHaveHearingDetails_HearingLocationRequiredViolations1() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        List<HearingLocation> locations = new ArrayList<>();
        HearingLocation location1 = new HearingLocation();
        location1.setLocationId(null);
        location1.setLocationType(null);
        locations.add(location1);
        hearingDetails.setHearingLocations(locations);
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains(ValidationError.LOCATION_ID_EMPTY));
        assertTrue(validationErrors.contains("Unsupported type for locationType"));
    }

    @Test
    void shouldHaveHearingDetails_HearingLocationDetailsViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationType("cluster");
        hearingLocation.setLocationId("");
        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(hearingLocation);
        hearingDetails.setHearingLocations(hearingLocations);
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.LOCATION_ID_EMPTY));
    }

    @Test
    void shouldHave_NoHearingDetailsViolation() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldHave_NoCaseDetailsViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldHave_CaseDetailsViolations() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("");
        caseDetails.setCaseRef("");
        caseDetails.setRequestTimeStamp(null);
        caseDetails.setExternalCaseReference("externalCaseReferenceexternalCaseReferenceexternalCaseReferenceexternal"
                                                 + "CaseReference");
        caseDetails.setCaseDeepLink("abc");
        caseDetails.setHmctsInternalCaseName("");
        caseDetails.setPublicCaseName("");
        caseDetails.setCaseCategories(new ArrayList<>());
        caseDetails.setCaseManagementLocationCode("");
        caseDetails.setCaseRestrictedFlag(null);
        caseDetails.setCaseSlaStartDate(null);
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(13, violations.size());
        assertTrue(validationErrors.contains(ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID));
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
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("externalCaseReferenceexternalCaseReferenceexternalCaseReferenceexternal"
                                      + "CaseReference");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        CaseDetails caseDetails = TestingUtil.caseDetails();
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
    }

    @Test
    void shouldHave_HearingResponseViolations() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID("a".repeat(41));
        hearingResponse.setHearingCancellationReason("a".repeat(41));
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(4, violations.size());
        assertTrue(validationErrors.contains(ValidationError.LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.HEARING_CANCELLATION_REASON_MAX_LENGTH));
        assertTrue(validationErrors.contains("Unsupported type for laCaseStatus"));
        assertTrue(validationErrors.contains("Unsupported type for listingStatus"));
    }

    @Test
    void shouldHave_HmiRequestDetailsViolations() {
        uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails requestDetails
                = new uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails();
        requestDetails.setHearingRequestId("a".repeat(31));
        requestDetails.setHearingGroupRequestId("b".repeat(31));
        Set<ConstraintViolation<uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails>> violations
                = validator.validate(requestDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains(ValidationError.HEARING_GROUP_REQUEST_ID_MAX_LENGTH));
        assertTrue(validationErrors.contains(ValidationError.HEARING_REQUEST_ID_MAX_LENGTH));
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

    @Test
    void shouldFailAsRequestDetailsNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_REQUEST_DETAILS));
    }

    @Test
    void shouldFailAsHearingDetailsNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_HEARING_DETAILS));
    }

    @Test
    void shouldFailAsCaseDetailsCaseCategoriesNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.getCaseDetails().setCaseCategories(new ArrayList<>());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_CASE_CATEGORIES));
    }

    @Test
    void shouldFailAsPanelRequirementsNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_PANEL_REQUIREMENTS));
    }

    @Test
    void shouldFailAsCaseDetailsNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_CASE_DETAILS));

    }

    @Test
    void shouldFailAsHearingLocationsNotPresent() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        createHearingRequest.setRequestDetails(TestingUtil.requestDetails());
        createHearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        createHearingRequest.setCaseDetails(TestingUtil.caseDetails());
        createHearingRequest.getHearingDetails().setHearingLocations(new ArrayList<>());
        createHearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<CreateHearingRequest>> violations = validator.validate(createHearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_HEARING_LOCATION));

    }

    @Test
    void whenInvalidCaseCategoryTypeIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("XXX");
        category.setCategoryType(null);
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains("Unsupported type for categoryType"));
        assertTrue(validationErrors.contains(CATEGORY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidCaseCategoryTypeIsEmpty() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("XXXX");
        category.setCategoryType("");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains("Unsupported type for categoryType"));
        assertTrue(validationErrors.contains(CATEGORY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidCaseCategoryValueIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue(null);
        category.setCategoryType(CaseCategoryType.CASETYPE.getLabel());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(CATEGORY_VALUE_EMPTY));
    }

    @Test
    void whenInvalidCaseCategoryValueIsEmpty() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("");
        category.setCategoryType(CaseCategoryType.CASETYPE.getLabel());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(CATEGORY_VALUE_EMPTY));
    }

    @Test
    void whenInvalidPartyIdIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(null);
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(PARTY_DETAILS_NULL_EMPTY));
    }

    @Test
    void whenInvalidPartyIdIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(PARTY_DETAILS_NULL_EMPTY));
    }

    @Test
    void whenInvalidPartyTypeIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(null);
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidPartyTypeIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType("");
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidPartyRoleIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole(null);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
    }

    @Test
    void whenInvalidPartyRoleIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
    }

}
