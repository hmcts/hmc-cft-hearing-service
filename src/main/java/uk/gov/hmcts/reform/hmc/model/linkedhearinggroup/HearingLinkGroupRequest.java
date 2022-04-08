package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
