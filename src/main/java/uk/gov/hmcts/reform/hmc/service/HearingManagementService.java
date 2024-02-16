package uk.gov.hmcts.reform.hmc.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;

public interface HearingManagementService {

    ResponseEntity<GetHearingResponse> getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(HearingRequest createHearingRequest, String deploymentId, String clientS2SToken);

    HearingResponse deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest,String clientS2SToken);

    HearingResponse updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest, String deploymentId,
                                         String clientS2SToken);

    GetHearingsResponse getHearings(String caseRefId, String caseStatus);

    GetHearingsResponse getEmptyHearingsResponse(String caseRefId);

    void sendResponse(String json, String hmctsServiceId, String deploymentId);

    ResponseEntity hearingCompletion(Long hearingId);

    String getStatus(Long hearingId);

}
