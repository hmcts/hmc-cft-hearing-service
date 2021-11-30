package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    void validateHearingRequest(HearingRequest hearingRequest);

    void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);
}
