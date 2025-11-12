package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Stream;

public class ManageRequestStateEnumPatternValidator implements
    ConstraintValidator<ManageRequestStateEnumPattern, String> {

    private List<String> acceptedValues;

    @Override
    public void initialize(ManageRequestStateEnumPattern annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .map(StringUtils::capitalize)
            .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value.toUpperCase());
    }
}
