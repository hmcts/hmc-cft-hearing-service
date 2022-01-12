package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingsGetResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(HearingRequest hearingRequest);

    void verifyAccess(String caseReference);

    void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);

    HearingsGetResponse validateGetHearingsRequest(String caseRefId, String caseStatus);

    void updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest);

    void validateHearingRequest(HearingRequest hearingRequest);

    void validateHearingRequest(UpdateHearingRequest hearingRequest);

}
