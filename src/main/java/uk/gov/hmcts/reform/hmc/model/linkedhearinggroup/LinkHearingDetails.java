package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkHearingDetails {

    @NotBlank(message = ValidationError.HEARING_ID_EMPTY)
    @Size(max = 30, message = ValidationError.HEARING_ID_LENGTH)
    private String hearingId;

    private int hearingOrder;
}
