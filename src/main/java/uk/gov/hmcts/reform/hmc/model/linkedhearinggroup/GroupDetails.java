package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class GroupDetails {
    @Size(max = 255, message = ValidationError.GROUP_NAME_LENGTH)
    private String groupName;

    @Size(max = 8, message = ValidationError.GROUP_REASON_LENGTH)
    private String groupReason;

    @NotNull(message = ValidationError.GROUP_LINK_TYPE_EMPTY)
    private String groupLinkType;

    @Size(max = 4000, message = ValidationError.GROUP_COMMENTS_LENGTH)
    private String groupComments;
}
