package uk.gov.hmcts.reform.hmc.validator;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HearingDurationReasonMaxLengthConstraintValidator implements
    ConstraintValidator<HearingDurationReasonMaxLengthConstraint, List<String>> {

    @Override
    public boolean isValid(List<String> reasonTypes, ConstraintValidatorContext context) {

        return false;
    }
}
