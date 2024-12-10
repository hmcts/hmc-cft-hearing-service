package uk.gov.hmcts.reform.hmc.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BooleanValidator implements ConstraintValidator<ValidBoolean, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (null == value || value.trim().isEmpty()) {
            return true; // Skip if null or empty; handled by NotNullNorEmpty
        }
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }
}
