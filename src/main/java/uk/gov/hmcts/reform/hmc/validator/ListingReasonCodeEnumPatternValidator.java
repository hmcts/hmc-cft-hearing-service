package uk.gov.hmcts.reform.hmc.validator;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ListingReasonCodeEnumPatternValidator implements ConstraintValidator<ListingReasonCodeEnumPattern,String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(final ListingReasonCodeEnumPattern annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
            .map(Enum::toString)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value);
    }

}
