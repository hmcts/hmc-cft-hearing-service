package uk.gov.hmcts.reform.hmc.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

@Data
@NoArgsConstructor
public class SupportRequestResponse {

    @NotBlank(message = ValidationError.HEARING_ID_EMPTY)
    @Size(max = 30, message = ValidationError.HEARING_ID_LENGTH)
    private String hearingId;

    @NotEmpty(message = ValidationError.STATUS_EMPTY)
    @EnumPattern(enumClass = ManageRequestStatus.class, fieldName = "status")
    @Schema(allowableValues = "successful, failure")
    private String status;

    @Size(max = 2000, message = ValidationError.MESSAGE_LENGTH)
    private String message;

}
