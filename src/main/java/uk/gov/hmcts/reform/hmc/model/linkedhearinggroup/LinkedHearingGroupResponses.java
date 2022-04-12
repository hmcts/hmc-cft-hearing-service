package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LinkedHearingGroupResponses {

    private GroupDetails groupDetails;

    private List<LinkedHearingGroupResponse> hearingsInGroup;
}
