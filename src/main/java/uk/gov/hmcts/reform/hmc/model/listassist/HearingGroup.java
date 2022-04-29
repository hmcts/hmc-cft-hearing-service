package uk.gov.hmcts.reform.hmc.model.listassist;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class HearingGroup {
    private String groupClientReference;
    private String groupName;
    private String groupReason;
    private String groupComment;
    private String groupLinkType;
    private String groupStatus;
    private ArrayList<CaseListing> groupHearings;
}
