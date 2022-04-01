package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull(message = ValidationError.VERSION_NUMBER_NULL_EMPTY)
    private Integer versionNumber;
}
