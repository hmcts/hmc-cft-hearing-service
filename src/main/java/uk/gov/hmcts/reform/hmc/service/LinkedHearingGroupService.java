package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;

public interface LinkedHearingGroupService {

    void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

    void deleteLinkedHearingGroup(Long hearingGroupId);

    void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest);

    GetLinkedHearingGroupResponse getLinkedHearingGroupDetails(String requestId);
}
