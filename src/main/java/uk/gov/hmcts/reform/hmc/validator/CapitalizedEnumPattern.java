package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CapitalizedCaseEnumPatternValidator.class)
public @interface CapitalizedEnumPattern {
    Class<? extends Enum<?>> enumClass();
    String fieldName();
    String message() default "Unsupported type for {fieldName}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
