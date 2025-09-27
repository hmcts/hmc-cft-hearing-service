package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.LuhnCheck;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestAction;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestState;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;
import uk.gov.hmcts.reform.hmc.validator.ManageRequestStateEnumPattern;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    @ManageRequestStateEnumPattern(enumClass = ManageRequestState.class, fieldName = "state")
    @Schema(allowableValues = "CANCELLED, COMPLETED, ADJOURNED")
    private String state;

    @NotNull(message = ValidationError.INVALID_SUPPORT_REQUEST_NOTES)
    @Size(max = 5000, message = ValidationError.MANAGE_EXCEPTION_NOTES_LENGTH)
    private String notes;

}
