package uk.gov.hmcts.reform.hmc.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = HearingDurationReasonMaxLengthValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface HearingDurationReasonMaxLengthConstraint {
    String message() default "The NonStandardHearingDurationReasonType values must not be more than 70 characters long";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
