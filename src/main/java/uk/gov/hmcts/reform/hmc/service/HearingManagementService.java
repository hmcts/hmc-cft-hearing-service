package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.HearingRequest;

public interface HearingManagementService {

    void validateHearingRequest(HearingRequest hearingRequest);

    void getHearingRequest(String hearingId);
}
