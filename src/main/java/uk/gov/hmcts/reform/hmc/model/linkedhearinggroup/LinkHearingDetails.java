package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkHearingDetails {

    @NotBlank(message = ValidationError.HEARING_ID_EMPTY)
    @Size(max = 30, message = ValidationError.HEARING_ID_LENGTH)
    private String hearingId;

    private int hearingOrder;
}
