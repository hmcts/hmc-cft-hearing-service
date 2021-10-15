package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CategoryType;
import uk.gov.hmcts.reform.hmc.model.Dow;
import uk.gov.hmcts.reform.hmc.model.DowUnavailabilityType;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.LocationId;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RequirementType;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumPatternValidatorTest {

    static Validator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenInvalidLocationIdIsNull() {
        HearingLocation location = new HearingLocation();
        location.setLocationId(null);
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationIdIsEmpty() {
        HearingLocation location = new HearingLocation();
        location.setLocationId("");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationId() {
        HearingLocation location = new HearingLocation();
        location.setLocationId("Loc");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenValidLocationId() {
        HearingLocation location = new HearingLocation();
        location.setLocationId(LocationId.COURT.toString());
        location.setLocationType("LocType");
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
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for categoryType", validationErrors.get(0));
    }

    @Test
    void whenInvalidCaseCategoryIsEmpty() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        category.setCategoryType("");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for categoryType", validationErrors.get(0));
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
        category.setCategoryType(CategoryType.CASESUBTYPE.toString());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidRequirementTypeIsNull() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType(null);
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
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
    void whenInvalidPartyDetailsIsEmpty() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType("");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for partyType", validationErrors.get(0));
    }

    @Test
    void whenInvalidPartyDetailsIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(null);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for partyType", validationErrors.get(0));
    }

    @Test
    void whenInvalidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType("IND1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for partyType", validationErrors.get(0));
    }

    @Test
    void whenValidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(PartyType.IND.toString());
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDowIsNull() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DowUnavailabilityType.ALL.toString());
        unavailabilityDow.setDow(null);
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenInValidUnavailabilityDowIsEmpty() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DowUnavailabilityType.ALL.toString());
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
        unavailabilityDow.setDowUnavailabilityType(DowUnavailabilityType.ALL.toString());
        unavailabilityDow.setDow(Dow.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDow() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DowUnavailabilityType.ALL.toString());
        unavailabilityDow.setDow("January");
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityType() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType("dow");
        unavailabilityDow.setDow(Dow.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityTypeIsNull() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(null);
        unavailabilityDow.setDow(Dow.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityTypeIsEmpty() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType("");
        unavailabilityDow.setDow(Dow.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }


    @Test
    void whenValidUnavailabilityDowUnavailabilityType() {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DowUnavailabilityType.ALL.toString());
        unavailabilityDow.setDow(Dow.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDow>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

}
