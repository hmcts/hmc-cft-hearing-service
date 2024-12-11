package uk.gov.hmcts.reform.hmc.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullNorEmptyValidator implements ConstraintValidator<NotNullNorEmpty, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value != null && !value.toString().trim().isEmpty();
    }
}
