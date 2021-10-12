package uk.gov.hmcts.reform.hmc.validator;

import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_SUB_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AUTHORISATION_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FACILITIES_REQUIRED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FACILITIES_REQUIRED_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NON_STANDARD_HEARING_DURATION_REASONS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.NON_STANDARD_HEARING_DURATION_REASON_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PANEL_SPECIALISMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PANEL_SPECIALISMS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REASONABLE_ADJUSTMENTS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REASONABLE_ADJUSTMENTS_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ROLE_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ROLE_TYPE_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.AUTHORISATION_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.FACILITIES_REQUIRED_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;

public class ListMaxLengthValidator implements ConstraintValidator<ListMaxLength, List<String>> {

    String listName = "";

    @Override
    public void initialize(ListMaxLength constraintAnnotation) {
        listName = constraintAnnotation.ListName();
    }

    @Override
    public boolean isValid(List<String> list, ConstraintValidatorContext context) {
        if (isListNotNullAndEmpty(list)) {
            if (listName.equals(NON_STANDARD_HEARING_DURATION_REASONS)) {
                for (String element : list) {
                    if (element.length() > NON_STANDARD_HEARING_DURATION_REASON_TYPE_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(
                            NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }
        } else if (listName.equals(ROLE_TYPE)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (element.length() > ROLE_TYPE_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(ROLE_TYPE_MAX_LENGTH_MSG).addConstraintViolation();
                        return false;
                    }
                }
            }
        } else if (listName.equals(AUTHORISATION_TYPE)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (element.length() > AUTHORISATION_TYPE_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(AUTHORISATION_TYPE_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }
        } else if (listName.equals(AUTHORISATION_SUB_TYPE)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (element.length() > AUTHORISATION_SUB_TYPE_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }
        } else if (listName.equals(PANEL_SPECIALISMS)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (element.length() > PANEL_SPECIALISMS_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(PANEL_SPECIALISMS_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }
        } else if (listName.equals(FACILITIES_REQUIRED)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (element.length() > FACILITIES_REQUIRED_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(FACILITIES_REQUIRED_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }

        } else if (listName.equals(REASONABLE_ADJUSTMENTS)) {
            if (isListNotNullAndEmpty(list)) {
                for (String element : list) {
                    if (null != element && element.length() > REASONABLE_ADJUSTMENTS_MAX_LENGTH) {
                        context.buildConstraintViolationWithTemplate(REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG)
                            .addConstraintViolation();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isListNotNullAndEmpty(List<String> list) {
        return list != null && !list.isEmpty();
    }

}
