package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkedHearingGroupResponses;

public interface LinkedHearingGroupService {

    void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

    void deleteLinkedHearingGroup(Long hearingGroupId);

    void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest);

    LinkedHearingGroupResponses getLinkedHearingGroupDetails(String requestId);
}
