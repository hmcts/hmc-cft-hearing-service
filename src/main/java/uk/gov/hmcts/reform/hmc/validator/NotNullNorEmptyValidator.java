package uk.gov.hmcts.reform.hmc.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullNorEmptyValidator implements ConstraintValidator<NotNullNorEmpty, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.trim().isEmpty();
    }
}
