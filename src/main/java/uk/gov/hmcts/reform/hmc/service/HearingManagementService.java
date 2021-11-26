package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    void deleteHearingRequest(String hearingId, DeleteHearingRequest deleteRequest);
}
