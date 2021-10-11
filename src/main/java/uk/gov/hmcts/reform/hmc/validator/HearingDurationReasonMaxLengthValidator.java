package uk.gov.hmcts.reform.hmc.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.reform.hmc.constants.Constants.NON_STANDARD_HEARING_DURATION_REASON_TYPE_MAX_LENGTH;

public class HearingDurationReasonMaxLengthValidator implements
    ConstraintValidator<HearingDurationReasonMaxLengthConstraint, List<String>> {

    @Override
    public boolean isValid(List<String> reasonTypes, ConstraintValidatorContext context) {
        for (String reasonType : reasonTypes) {
            if (reasonType.length() > NON_STANDARD_HEARING_DURATION_REASON_TYPE_MAX_LENGTH) {
                return false;
            }
        }
        return true;
    }
}
