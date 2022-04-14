package uk.gov.hmcts.reform.hmc.model.linkedhearinggroup;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetLinkedHearingGroupResponse {

    private GroupDetails groupDetails;

    private List<LinkedHearingDetails> hearingsInGroup;
}
