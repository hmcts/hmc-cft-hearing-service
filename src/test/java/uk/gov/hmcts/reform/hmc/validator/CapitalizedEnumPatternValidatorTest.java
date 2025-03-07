package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapitalizedEnumPatternValidatorTest {

    static Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "MoNdAy",
        "TUEsday", "wednesday", "THuRSDAY", "FridaY", "saturday", "sUNDAY"})
    void whenInvalidCaseUnavailabilityDow(String dowValue) {
        final Set<ConstraintViolation<UnavailabilityDow>> violations =
                createAndValidateUnavailabilityDow(dowValue);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"})
    void whenValidCaseUnavailabilityDow(String dowValue) {
        final Set<ConstraintViolation<UnavailabilityDow>> violations =
                createAndValidateUnavailabilityDow(dowValue);
        assertTrue(violations.isEmpty());
    }

    private Set<ConstraintViolation<UnavailabilityDow>> createAndValidateUnavailabilityDow(String dowValue) {
        UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnAvailableType.ALL.toString());
        unavailabilityDow.setDow(dowValue);
        return validator.validate(unavailabilityDow);
    }
}
