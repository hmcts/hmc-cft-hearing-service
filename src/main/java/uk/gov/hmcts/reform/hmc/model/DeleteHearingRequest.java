package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CANCELLATION_REASON_CODE_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteHearingRequest {

    @NotNull(message = ValidationError.INVALID_CANCELLATION_REASON_CODE)
    private List<@Size(max = 100, message = CANCELLATION_REASON_CODE_MAX_LENGTH_MSG) String>
        cancellationReasonCode;

    @NotNull(message = ValidationError.INVALID_VERSION_NUMBER)
    private Integer versionNumber;
}
