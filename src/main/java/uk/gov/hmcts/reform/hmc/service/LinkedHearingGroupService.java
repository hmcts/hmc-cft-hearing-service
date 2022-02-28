package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedHearingGroup.HearingLinkGroupRequest;

public interface LinkedHearingGroupService {

    void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

}
