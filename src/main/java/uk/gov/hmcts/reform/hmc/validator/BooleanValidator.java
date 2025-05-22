package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BooleanValidator implements ConstraintValidator<ValidBoolean, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (null == value || value.toString().trim().isEmpty()) {
            return true; // Skip if null or empty; handled by NotNullNorEmpty
        }
        return "true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString());
    }
}
