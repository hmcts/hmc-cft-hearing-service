package uk.gov.hmcts.reform.hmc.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
