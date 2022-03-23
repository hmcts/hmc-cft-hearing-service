package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;

public interface LinkedHearingGroupService {

    void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest);

}
