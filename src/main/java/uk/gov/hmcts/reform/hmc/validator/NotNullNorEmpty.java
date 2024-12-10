package uk.gov.hmcts.reform.hmc.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = NotNullNorEmptyValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNullNorEmpty {
    String message() default "The field must not be null nor empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
