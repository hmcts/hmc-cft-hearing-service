package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ActualIndividualDetails {

    @NotNull(message = ValidationError.FIRST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.FIRST_NAME_MAX_LENGTH)
    private String firstName;

    @NotNull(message = ValidationError.LAST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.LAST_NAME_MAX_LENGTH)
    private String lastName;
}
