package uk.gov.hmcts.reform.hmc.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ListingReasonCodeEnumPatternValidator.class)
public @interface ListingReasonCodeEnumPattern {
    Class<? extends Enum<ListingReasonCode>> enumClass();
    String fieldName();
    String message() default "Unsupported type or value for {fieldName}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
