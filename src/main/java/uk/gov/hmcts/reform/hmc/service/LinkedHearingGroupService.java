package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.PreviousLinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;

import java.util.HashMap;
import java.util.List;

public interface LinkedHearingGroupService {

    HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

    void deleteLinkedHearingGroup(String requestId);

    void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest);

    GetLinkedHearingGroupResponse getLinkedHearingGroupResponse(String requestId);
}
