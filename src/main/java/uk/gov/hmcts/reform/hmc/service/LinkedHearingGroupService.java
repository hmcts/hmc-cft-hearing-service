package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;

public interface LinkedHearingGroupService {

    HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

    void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest);

}
