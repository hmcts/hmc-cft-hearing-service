package uk.gov.hmcts.reform.hmc.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ListMaxLengthValidator.class)
public @interface ListMaxLength {

    String message() default "Please enter a valid element for this field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String ListName();
}
