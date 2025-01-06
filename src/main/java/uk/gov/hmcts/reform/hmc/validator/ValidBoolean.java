package uk.gov.hmcts.reform.hmc.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = BooleanValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBoolean {
    String message() default "The field must be a boolean value (true or false)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
