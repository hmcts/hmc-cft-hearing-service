package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapitalizedCaseEnumPatternValidator implements ConstraintValidator<CapitalizedEnumPattern, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(CapitalizedEnumPattern annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
            .map(enumValue -> enumValue.name().toLowerCase())
            .map(StringUtils::capitalize)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return acceptedValues.contains(value);
    }
}
