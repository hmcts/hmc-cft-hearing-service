package uk.gov.hmcts.reform.hmc.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = HearingCodeEnumPatternValidator.class)
public @interface HearingCodeEnumPattern {
    Class<? extends Enum<?>> enumClass();
    String fieldName();
    String message() default "Unsupported type or value for Hearing case status code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
