package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_SUPPORT_REQUEST_NOTES;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.MANAGE_EXCEPTION_ACTION_EMPTY;

class SupportRequestValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeEach
    void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void inValidateWhenRequestIsNull() {
        SupportRequest supportRequest = new SupportRequest();

        Set<ConstraintViolation<SupportRequest>> violations = validator.validate(supportRequest);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(5);
        assertThat(validationErrors).contains(CASE_REF_EMPTY)
            .contains(HEARING_ID_EMPTY)
            .contains("Unsupported type for action")
            .contains(INVALID_SUPPORT_REQUEST_NOTES)
            .contains(MANAGE_EXCEPTION_ACTION_EMPTY);
    }

    @Test
    void shouldHaveSupportRequestViolations() {
        SupportRequest supportRequest = generateSupportRequestWithInvalidValues();

        Set<ConstraintViolation<SupportRequest>> violations = validator.validate(supportRequest);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(5);
        assertThat(validationErrors).contains(HEARING_ID_LENGTH)
            .contains(INVALID_CASE_REFERENCE)
            .contains("Unsupported type for state")
            .contains(INVALID_SUPPORT_REQUEST_NOTES);
    }

    @Test
    void shouldHaveSupportRequestViolationsForEmptyValues() {
        SupportRequest supportRequest = generateSupportRequestWithEmptyValues();

        Set<ConstraintViolation<SupportRequest>> violations = validator.validate(supportRequest);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(2);
        assertThat(validationErrors).contains(HEARING_ID_EMPTY)
            .contains(CASE_REF_EMPTY);
    }

    @Test
    void shouldHaveSupportRequestViolationsForNotesLength() {
        SupportRequest supportRequest = generateValidSupportRequest();
        supportRequest.setNotes("A".repeat(5001));

        Set<ConstraintViolation<SupportRequest>> violations = validator.validate(supportRequest);
        List<String> validationErrors = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
        assertThat(violations).isNotEmpty().hasSize(1);
        assertThat(validationErrors).contains(ValidationError.MANAGE_EXCEPTION_NOTES_LENGTH);
    }

    private SupportRequest generateValidSupportRequest() {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setHearingId("2000000000");
        supportRequest.setCaseRef("9372710950276233");
        supportRequest.setAction(ManageRequestAction.ROLLBACK.name());
        supportRequest.setState(HearingStatus.CANCELLED.name());
        supportRequest.setNotes("INC 123456");
        return  supportRequest;
    }

    private SupportRequest generateSupportRequestWithEmptyValues() {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setAction(ManageRequestAction.ROLLBACK.name());
        supportRequest.setState(HearingStatus.CANCELLED.name());
        supportRequest.setNotes("INC 123456");
        return  supportRequest;
    }

    private SupportRequest generateSupportRequestWithInvalidValues() {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setHearingId("200000000000000000000000000000000");
        supportRequest.setCaseRef("123456789012345678901234567890123");
        supportRequest.setAction("invalid_action");
        supportRequest.setState("INVALID_STATE");
        supportRequest.setNotes(null);
        return  supportRequest;
    }

}
