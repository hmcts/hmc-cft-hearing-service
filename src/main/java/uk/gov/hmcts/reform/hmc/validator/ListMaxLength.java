package uk.gov.hmcts.reform.hmc.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ListMaxLengthValidator.class)
public @interface ListMaxLength {

    String message() default "{ListName} has invalid data}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String ListName();
}
