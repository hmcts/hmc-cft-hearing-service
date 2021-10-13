package uk.gov.hmcts.reform.hmc.validator;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumPatternValidator implements ConstraintValidator<EnumPattern, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(EnumPattern annotation) {
        acceptedValues = new ArrayList<String>();
        Class<? extends Enum<?>> enumClass = annotation.enumClass();
        Enum[] enumValArr = enumClass.getEnumConstants();
        for (Enum enumVal : enumValArr) {
            acceptedValues.add(enumVal.toString().toUpperCase());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return acceptedValues.contains(value.toUpperCase());
    }

}
