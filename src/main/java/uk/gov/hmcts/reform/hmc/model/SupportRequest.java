package uk.gov.hmcts.reform.hmc.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.LuhnCheck;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class SupportRequest {

    @NotBlank(message = ValidationError.HEARING_ID_EMPTY)
    @Size(max = 30, message = ValidationError.HEARING_ID_LENGTH)
    private String hearingId;

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @LuhnCheck(message = ValidationError.INVALID_CASE_REFERENCE, ignoreNonDigitCharacters = false)
    private String caseRef;

    @NotEmpty(message = ValidationError.MANAGE_EXCEPTION_ACTION_EMPTY)
    @EnumPattern(enumClass = ManageRequestAction.class, fieldName = "action")
    @Schema(allowableValues = "rollback, final_state_transition")
    private String action;

    @Size(max = 5000, message = ValidationError.MANAGE_EXCEPTION_NOTES_LENGTH)
    private String notes;

    @EnumPattern(enumClass = HearingStatus.class, fieldName = "state")
    @Schema(allowableValues = "CANCELLED, COMPLETED, ADJOURNED")
    private String state;

}
