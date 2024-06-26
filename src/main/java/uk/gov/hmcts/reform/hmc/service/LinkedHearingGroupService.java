package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;

public interface LinkedHearingGroupService {

    HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest, String clientS2SToken);

    void deleteLinkedHearingGroup(String requestId, String clientS2SToken);

    void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest, String clientS2SToken);

    GetLinkedHearingGroupResponse getLinkedHearingGroupResponse(String requestId);
}
