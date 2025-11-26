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
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCaseStatus;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingCodeEnumPatternValidatorTest {

    static Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(HearingCodeEnumPatternValidatorTest.class);

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenInvalidHearingCodeNumber() {
        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode("11");
        Set<ConstraintViolation<HearingCaseStatus>> violations = validator.validate(hearingCaseStatus);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type or value for hearing case status code", validationErrors.get(0));
    }

    @Test
    void whenInvalidHearingCodeIsNull() {
        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode(null);
        Set<ConstraintViolation<HearingCaseStatus>> violations = validator.validate(hearingCaseStatus);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("Hearing code can not be null or empty"));
        assertTrue(validationErrors.contains("Unsupported type or value for hearing case status code"));
    }

    @Test
    void whenInvalidHearingCodeString() {
        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode(HearingCode.LISTED.name());
        Set<ConstraintViolation<HearingCaseStatus>> violations = validator.validate(hearingCaseStatus);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type or value for hearing case status code", validationErrors.get(0));
    }

    @Test
    void whenValidHearingCode() {
        HearingCaseStatus hearingCaseStatus = new HearingCaseStatus();
        hearingCaseStatus.setCode("100");
        Set<ConstraintViolation<HearingCaseStatus>> violations = validator.validate(hearingCaseStatus);
        assertTrue(violations.isEmpty());
    }
}
