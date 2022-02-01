package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingsGetResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(CreateHearingRequest createHearingRequest);

    void verifyAccess(String caseReference);

    HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

    GetHearingsResponse getHearings(String caseRefId, String caseStatus);

    void validateHearingRequest(HearingRequest hearingRequest);

    void validateHearingRequest(UpdateHearingRequest hearingRequest);

    void sendResponse(String json);

    void sendRequestToHmi(Long hearingId, HearingRequest hearingRequest);
  
}
