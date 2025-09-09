package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageRequestStateEnumPatternValidatorTest {

    static Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE_SUBMITTED", "LISTED", "AWAITING_LISTING,"
                 + "ADJOURNED, CANCELLATION_REQUESTED, CANCELLATION_SUBMITTED, CLOSED, EXCEPTION"})
    void whenInvalidManageRequestStateValues(String stateValue) {
        final Set<ConstraintViolation<SupportRequest>> violations =
            createAndValidateSupportRequest(stateValue);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for state", validationErrors.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELLED", "COMPLETED", "ADJOURNED"})
    void whenValidManageRequestStateValues(String stateValue) {
        final Set<ConstraintViolation<SupportRequest>> violations =
            createAndValidateSupportRequest(stateValue);
        assertTrue(violations.isEmpty());
    }

    private Set<ConstraintViolation<SupportRequest>> createAndValidateSupportRequest(String stateValue) {
        SupportRequest request = new SupportRequest();
        request.setCaseRef("9856815055686759");
        request.setHearingId("2000000000");
        request.setAction(ManageRequestAction.ROLLBACK.label);
        request.setState(stateValue);
        request.setNotes("Inc1234");
        return validator.validate(request);
    }
}
