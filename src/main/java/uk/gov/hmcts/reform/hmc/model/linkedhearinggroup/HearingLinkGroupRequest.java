package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;

@Data
@NoArgsConstructor
public class HearingLinkGroupRequest {

    @Valid
    @NotNull(message = ValidationError.GROUP_DETAILS)
    private GroupDetails groupDetails;

    @Valid
    @NotEmpty(message = ValidationError.HEARINGS_IN_GROUP)
    @Size(min = 2, message = ValidationError.HEARINGS_IN_GROUP_SIZE)
    private List<LinkHearingDetails> hearingsInGroup;

}
