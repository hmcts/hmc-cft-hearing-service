package uk.gov.hmcts.reform.hmc.validator;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ManageRequestStateEnumPatternValidator implements
    ConstraintValidator<ManageRequestStateEnumPattern, String> {

    private List<String> acceptedValues;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value.toUpperCase());
    }
}
