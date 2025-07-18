package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ManageExceptionRequest {

    @Valid
    @NotNull(message = ValidationError.INVALID_SUPPORT_REQUEST_DETAILS)
    private List<SupportRequest> supportRequest;

}
