package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_VALUE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_LOCATION_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_DETAILS_NULL_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_TYPE_EMPTY;


class BeanValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(BeanValidatorTest.class);

    private static ValidatorFactory validatorFactory;
    private static Validator validator;
    String invalidUtf8 = "Text\uFFFF";

    @BeforeEach
    void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void shouldHaveHearingDetailsViolations() {
        HearingDetails hearingDetails = generateHearingDetails();

        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(9);
        assertThat(validationErrors).contains(ValidationError.AUTO_LIST_FLAG_NULL_EMPTY)
        .contains(ValidationError.HEARING_TYPE_MAX_LENGTH)
        .contains(ValidationError.DURATION_MIN_VALUE)
        .contains(ValidationError.HEARING_PRIORITY_TYPE)
        .contains(ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE)
        .contains(ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH)
        .contains(ValidationError.HEARING_LOCATION_EMPTY)
        .contains(ValidationError.HEARING_CHANNEL_EMPTY);
    }

    @ParameterizedTest
    @CsvSource({
        "null, 9, true, false",
        "'', 9, true, false",
        "'TTT', 10, false, true",
        "'true', 9, false, false",
        "'false', 9, false, false",
    })
    void shouldCheckIsAPanelViolations(String isAPanelFlag, int expectedViolationCount,
                                            boolean containsNullEmptyError, boolean containsTypeError) {
        HearingDetails hearingDetails = generateHearingDetails();
        hearingDetails.setIsAPanelFlag(isAPanelFlag.equals("null") ? null : isAPanelFlag);

        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(expectedViolationCount);

        assertThat(validationErrors.contains(ValidationError.IS_A_PANEL_FLAG_INVALID_TYPE))
            .isEqualTo(containsTypeError);
    }

    private HearingDetails generateHearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(null);
        hearingDetails.setHearingType(
            "HearingType more than 40HearingType more than 40HearingType more than 40HearingType more than 40");
        hearingDetails.setDuration(-1);
        hearingDetails.setHearingPriorityType("");
        hearingDetails.setNumberOfPhysicalAttendees(-1);
        hearingDetails.setLeadJudgeContractType("Lead judge contractLead judge contractLead judge contractLead judge "
                                                    + "contractLead judge contractLead judge contract");
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        return hearingDetails;
    }

    @Test
    void shouldHaveHearingDetails_NonStandardHearingDurationReasonsViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        String reason = "NonStandardHearingDurationReasonsnonStandardHearingDurationReasonsnonStandardHearingDura ";
        hearingDetails.setNonStandardHearingDurationReasons(List.of(reason));
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG);
    }

    @Test
    void shouldHaveHearingDetails_HearingLocationRequiredViolations() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        hearingDetails.setHearingLocations(null);
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(2);
        assertThat(validationErrors).contains(HEARING_LOCATION_EMPTY);
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
        assertThat(violations).isNotEmpty().hasSize(2);
        assertThat(validationErrors).contains(ValidationError.LOCATION_ID_EMPTY)
            .contains("Unsupported type for locationType");
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
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.LOCATION_ID_EMPTY);
    }

    @Test
    void shouldHave_NoHearingDetailsViolation() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHave_NoCaseDetailsViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHave_CaseDetailsViolations() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("");
        caseDetails.setCaseRef("");
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
        assertThat(violations).isNotEmpty().hasSize(12);
        assertThat(validationErrors).contains(ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
            .contains(ValidationError.CASE_REF_EMPTY)
            .contains(ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH)
            .contains(ValidationError.CASE_DEEP_LINK_INVALID)
            .contains(ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY)
            .contains(ValidationError.PUBLIC_CASE_NAME_EMPTY)
            .contains(ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY)
            .contains(ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY);

    }

    @Test
    void shouldHave_CaseDetails_ValidRegExViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        caseDetails.setHmctsInternalCaseName("Valid Text 123!@#");
        caseDetails.setPublicCaseName("Valid Text 123!@#");
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldHave_CaseDetails_RegExViolations() {
        CaseDetails caseDetails = TestingUtil.caseDetails();
        caseDetails.setHmctsInternalCaseName(invalidUtf8);
        caseDetails.setPublicCaseName(invalidUtf8);
        Set<ConstraintViolation<CaseDetails>> violations = validator.validate(caseDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_HMCTS_INTERNAL_CASE_NAME));
        assertTrue(validationErrors.contains(ValidationError.INVALID_PUBLIC_CASE_NAME));
    }

    @Test
    void shouldHave_PartyDetails_RegExViolations() {
        List<PartyDetails> partyDetailsList = TestingUtil.partyDetailsWith2Parties(true);
        partyDetailsList.get(0).getIndividualDetails().setFirstName(invalidUtf8);
        partyDetailsList.get(0).getIndividualDetails().setLastName(invalidUtf8);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetailsList.get(0));
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_FIRST_NAME));
        assertTrue(validationErrors.contains(ValidationError.INVALID_LAST_NAME));
    }

    @Test
    void shouldHave_OrgDetails_RegExViolations() {
        OrganisationDetails organisationDetails = TestingUtil.organisationDetails();
        organisationDetails.setName(invalidUtf8);
        Set<ConstraintViolation<OrganisationDetails>> violations = validator.validate(organisationDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(validationErrors.contains(ValidationError.INVALID_ORGANISATION_NAME));
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
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.CATEGORY_VALUE);
    }

    @Test
    void shouldFailAsRequestDetailsVersionNumberNotPresent() {
        RequestDetails requestDetails = TestingUtil.requestDetails();
        Set<ConstraintViolation<RequestDetails>> violations = validator.validate(requestDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.VERSION_NUMBER_NULL_EMPTY);
    }

    @Test
    void shouldHave_HearingResponseViolations() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID("a".repeat(41));
        hearingResponse.setHearingCancellationReason("a".repeat(41));
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(4);
        assertThat(validationErrors).contains(ValidationError.LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH)
            .contains(ValidationError.HEARING_CANCELLATION_REASON_MAX_LENGTH)
            .contains("Unsupported type for laCaseStatus")
            .contains(ValidationError.HEARING_STATUS_CODE_NULL);
    }

    @Test
    void shouldHave_HearingResponseListingStatusMaxLengthViolation() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID("a".repeat(40));
        hearingResponse.setHearingCancellationReason("a".repeat(40));
        hearingResponse.setLaCaseStatus(ListAssistCaseStatus.AWAITING_LISTING.name());
        hearingResponse.setListingStatus("a".repeat(31));
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.HEARING_STATUS_CODE_LENGTH);
    }

    @Test
    void shouldPass_HearingResponse() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID("a".repeat(40));
        hearingResponse.setHearingCancellationReason("a".repeat(40));
        hearingResponse.setLaCaseStatus(ListAssistCaseStatus.AWAITING_LISTING.name());
        hearingResponse.setListingStatus("a".repeat(30));
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isEmpty();
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
        assertThat(violations).isNotEmpty().hasSize(2);
        assertThat(validationErrors).contains(ValidationError.HEARING_GROUP_REQUEST_ID_MAX_LENGTH)
            .contains(ValidationError.HEARING_REQUEST_ID_MAX_LENGTH);
    }

    @Test
    void shouldFailAsRequestDetailsNotPresent() {
        UpdateHearingRequest hearingRequest = new UpdateHearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_REQUEST_DETAILS);
    }

    @Test
    void shouldFailAsHearingDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_HEARING_DETAILS);
    }

    @Test
    void shouldFailAsCaseDetailsCaseCategoriesNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getCaseDetails().setCaseCategories(new ArrayList<>());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_CASE_CATEGORIES);
    }

    @Test
    void shouldFailAsPanelRequirementsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_PANEL_REQUIREMENTS);
    }

    @Test
    void shouldFailAsCaseDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_CASE_DETAILS);

    }

    @Test
    void shouldFailAsHearingLocationsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getHearingDetails().setHearingLocations(new ArrayList<>());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingRequest>> violations = validator.validate(hearingRequest);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.INVALID_HEARING_LOCATION);

    }

    @Test
    void whenInvalidCaseCategoryTypeIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("XXX");
        category.setCategoryType(null);
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(2);
        assertThat(validationErrors).contains("Unsupported type for categoryType")
            .contains(CATEGORY_TYPE_EMPTY);
    }

    @Test
    void whenInvalidCaseCategoryTypeIsEmpty() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("XXXX");
        category.setCategoryType("");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(2);
        assertThat(validationErrors).contains("Unsupported type for categoryType").contains(CATEGORY_TYPE_EMPTY);
    }

    @Test
    void whenInvalidCaseCategoryValueIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue(null);
        category.setCategoryType(CaseCategoryType.CASETYPE.getLabel());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(CATEGORY_VALUE_EMPTY);
    }

    @Test
    void whenInvalidCaseCategoryValueIsEmpty() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("");
        category.setCategoryType(CaseCategoryType.CASETYPE.getLabel());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(CATEGORY_VALUE_EMPTY);
    }

    @Test
    void whenInvalidPartyIdIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(null);
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(PARTY_DETAILS_NULL_EMPTY);
    }

    @Test
    void whenInvalidPartyIdIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(PARTY_DETAILS_NULL_EMPTY);
    }

    @Test
    void whenInvalidPartyTypeIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(null);
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(2);
        assertThat(validationErrors).contains("Unsupported type for partyType")
            .contains(PARTY_TYPE_EMPTY);
    }

    @Test
    void whenInvalidPartyTypeIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType("");
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(2);
        assertThat(validationErrors).contains("Unsupported type for partyType")
            .contains(PARTY_TYPE_EMPTY);
    }

    @Test
    void whenInvalidPartyRoleIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole(null);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(PARTY_ROLE_EMPTY);
    }

    @Test
    void whenInvalidPartyRoleIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isNotEmpty();
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertThat(violations).hasSize(1);
        assertThat(validationErrors).contains(PARTY_ROLE_EMPTY);
    }

    @Test
    void whenValidPartyRoleIs40Characters() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenInvalidPartyRoleIs41Characters() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("XXX1");
        partyDetails.setPartyType(PartyType.IND.getLabel());
        partyDetails.setPartyRole("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertThat(violations).hasSize(1);
        assertThat(violations.stream()
                       .map(ConstraintViolation::getMessage)
                       .anyMatch(msg -> msg.equals(PARTY_ROLE_MAX_LENGTH))).isTrue();
    }

    @Test
    void shouldSucceedWhenGroupReasonIsNullForHman146() {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupReason(null);
        groupDetails.setGroupName("groupName");
        groupDetails.setGroupComments("groupComments");
        groupDetails.setGroupLinkType("linkType");
        Set<ConstraintViolation<GroupDetails>> violations = validator.validate(groupDetails);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHave_NoHearingDetailsViolationAsHearingWindowOptional() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setHearingWindow(null);
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHave_NoHearingDetailsViolationAsListingAutoChangeReasonCodeOptional() {
        HearingDetails hearingDetails = TestingUtil.hearingDetails();
        hearingDetails.setListingAutoChangeReasonCode(null);
        hearingDetails.setPanelRequirements(TestingUtil.panelRequirements());
        Set<ConstraintViolation<HearingDetails>> violations = validator.validate(hearingDetails);
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertThat(violations).isEmpty();
    }

}
