package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(CreateHearingRequest createHearingRequest);

    void verifyAccess(String caseReference);

    void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    CreateHearingRequest validateGetHearingsRequest(String caseRefId, String caseStatus);

    void updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

    HmiSubmitHearingRequest sendRequestToHmi(Long hearingId, HearingRequest hearingRequest);

}
