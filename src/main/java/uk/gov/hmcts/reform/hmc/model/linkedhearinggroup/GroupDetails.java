package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class GroupDetails {
    @Size(max = 255, message = ValidationError.GROUP_NAME_LENGTH)
    private String groupName;

    @NotBlank(message = ValidationError.GROUP_REASON_EMPTY)
    @Size(max = 8, message = ValidationError.GROUP_REASON_LENGTH)
    private String groupReason;

    @NotNull(message = ValidationError.GROUP_LINK_TYPE_EMPTY)
    private LinkType groupLinkType;

    @Size(max = 4000, message = ValidationError.GROUP_COMMENTS_LENGTH)
    private String groupComments;
}
