package uk.gov.hmcts.reform.hmc.model.listassist;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class HearingGroup {
    private String groupClientReference;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupReason;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupComment;
    private String groupLinkType;
    private String groupStatus;
    private ArrayList<CaseListing> groupHearings;
}
