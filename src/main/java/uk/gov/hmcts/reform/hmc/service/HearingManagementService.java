package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

public interface HearingManagementService {

    HearingResponse saveHearingRequest(HearingRequest hearingRequest);

    void getHearingRequest(String hearingId);
}
