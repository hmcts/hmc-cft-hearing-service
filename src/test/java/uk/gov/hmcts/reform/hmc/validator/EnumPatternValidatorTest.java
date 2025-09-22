package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.LocationType;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RequirementType;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CATEGORY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LOCATION_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_ROLE_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTY_TYPE_EMPTY;

class EnumPatternValidatorTest {

    static Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(EnumPatternValidatorTest.class);

    private static final String FRIDAY = "Friday";

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenInvalidLocationTypeIsNull() {
        HearingLocation location = new HearingLocation();
        setLocation(location, "Id", null);
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationType", validationErrors.get(0));
    }

    private void setLocation(HearingLocation location, String id, String o) {
        location.setLocationId(id);
        location.setLocationType(o);
    }

    @Test
    void whenInvalidLocationTypeIsEmpty() {
        HearingLocation location = new HearingLocation();
        setLocation(location, "Id", "");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationType", validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationIdIsEmpty() {
        HearingLocation location = new HearingLocation();
        setLocation(location, "", LocationType.CLUSTER.toString());
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals(LOCATION_ID_EMPTY, validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationType() {
        HearingLocation location = new HearingLocation();
        setLocation(location, "Loc", "Loc");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationType", validationErrors.get(0));
    }

    @Test
    void whenValidLocationType() {
        HearingLocation location = new HearingLocation();
        location.setLocationType(LocationType.COURT.toString());
        location.setLocationId("LocType");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidCaseCategoryIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
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
    void whenInvalidCaseCategoryIsEmpty() {
        CaseCategory category = getCaseCategory();
        category.setCategoryType("");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("Unsupported type for categoryType"));
        assertTrue(validationErrors.contains(CATEGORY_TYPE_EMPTY));
    }

    private CaseCategory getCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        return category;
    }

    @Test
    void whenInvalidCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        category.setCategoryType("categoryType");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for categoryType", validationErrors.get(0));
    }

    @Test
    void whenValidCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("caseValue");
        category.setCategoryType(CaseCategoryType.CASESUBTYPE.toString());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidRequirementTypeIsNull() {
        PanelPreference panelPreference = getPanelPreference();
        panelPreference.setRequirementType(null);
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }

    private PanelPreference getPanelPreference() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        return panelPreference;
    }

    @Test
    void whenInvalidRequirementTypeIsEmpty() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType("");
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }

    @Test
    void whenInvalidRequirementType() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType("preference");
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }

    @Test
    void whenValidRequirementType() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType(RequirementType.MUSTINC.toString());
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidListAssistCaseStatusIsEmpty() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setLaCaseStatus("");
        hearingResponse.setListingStatus("Fixed");
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains("Unsupported type for laCaseStatus"));
    }

    @Test
    void whenInvalidListingStatusIsEmpty() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setLaCaseStatus(ListAssistCaseStatus.CASE_CLOSED.name());
        hearingResponse.setListingStatus("");
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains(ValidationError.HEARING_STATUS_CODE_NULL));
    }

    @Test
    void whenInvalidPartyDetailsIsEmpty() {
        PartyDetails partyDetails = getPartyDetails();
        partyDetails.setPartyType("");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
    }

    private PartyDetails getPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        return partyDetails;
    }

    @Test
    void whenInvalidPartyDetailsIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(null);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
    }

    @Test
    void whenInvalidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType("IND1");
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
    }

    @Test
    void whenValidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(PartyType.IND.toString());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDowIsNull() {
        UnavailabilityDow unavailabilityDow = getUnavailabilityDow();
        unavailabilityDow.setDow(null);
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    private UnavailabilityDow getUnavailabilityDow() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        return unavailabilityDow;
    }

    @Test
    void whenInValidUnavailabilityDowIsEmpty() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        unavailabilityDow.setDow("");
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenValidUnavailabilityDow() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        unavailabilityDow.setDow(FRIDAY);
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDow() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        unavailabilityDow.setDow("January");
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenValidUnavailabilityDowUnavailabilityType() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        unavailabilityDow.setDow(FRIDAY);
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

}
