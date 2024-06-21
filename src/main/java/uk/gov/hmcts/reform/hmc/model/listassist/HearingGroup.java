package uk.gov.hmcts.reform.hmc.model.listassist;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingGroup {
    private String groupClientReference;
    private String groupName;
    private String groupReason;
    private String groupComment;
    private String groupLinkType;
    private String groupStatus;
    private ArrayList<CaseListing> groupHearings;
}
