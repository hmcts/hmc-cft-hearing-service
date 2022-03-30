package uk.gov.hmcts.reform.hmc.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;

public interface HearingManagementService {

    ResponseEntity<GetHearingResponse> getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(CreateHearingRequest createHearingRequest);

    void verifyAccess(String caseReference);

    HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

    GetHearingsResponse getHearings(String caseRefId, String caseStatus);

    void sendResponse(String json);

    void sendRequestToHmiAndQueue(HearingRequest hearingRequest, Long hearingId, String messageType);

    void sendRequestToHmiAndQueue(DeleteHearingRequest hearingRequest,Long hearingId, String messageType);

    HmiSubmitHearingRequest test(Long hearingId, CreateHearingRequest hearingRequest);

    HmiSubmitHearingRequest test(Long hearingId, UpdateHearingRequest hearingRequest);
}
