package uk.gov.hmcts.reform.hmc.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.hmc.model.*;

public interface HearingManagementService {

    ResponseEntity<GetHearingResponse> getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(CreateHearingRequest createHearingRequest);

    void verifyAccess(String caseReference);

    HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

    GetHearingsResponse getHearings(String caseRefId, String caseStatus);

    void sendResponse(String json);

    void sendRequestToHmi(Long hearingId, HearingRequest hearingRequest);

    void sendRequestToHmi(DeleteHearingRequest hearingRequest);

}
