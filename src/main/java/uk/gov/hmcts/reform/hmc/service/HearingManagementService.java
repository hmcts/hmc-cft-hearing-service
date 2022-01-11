package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(HearingRequest hearingRequest);

    void verifyAccess(String caseReference);

    HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    HearingRequest validateGetHearingsRequest(String caseRefId, String caseStatus);

    void updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

}
