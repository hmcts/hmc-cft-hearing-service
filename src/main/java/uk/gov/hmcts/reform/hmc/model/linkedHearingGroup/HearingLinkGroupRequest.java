package uk.gov.hmcts.reform.hmc.model.linkedHearingGroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class HearingLinkGroupRequest extends HearingRequest {

    @Valid
    @NotEmpty(message = ValidationError.GROUP_DETAILS)
    private GroupDetails groupDetails;

    @Valid
    @NotEmpty(message = ValidationError.HEARINGS_IN_GROUP)
    @Size(min = 2, message = ValidationError.HEARINGS_IN_GROUP_SIZE)
    private List<LinkHearingDetails> hearingsInGroup;

}
