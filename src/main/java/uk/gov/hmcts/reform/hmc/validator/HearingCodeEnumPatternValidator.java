package uk.gov.hmcts.reform.hmc.validator;

import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HearingCodeEnumPatternValidator implements ConstraintValidator<HearingCodeEnumPattern, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(final HearingCodeEnumPattern annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Integer number = null;
        try {
            number = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return HearingCode.isValidNumber(number);
    }

}
