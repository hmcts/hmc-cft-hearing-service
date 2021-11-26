package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingsGetResponse;

public interface HearingManagementService {

    void validateHearingRequest(HearingRequest hearingRequest);

    HearingsGetResponse validateGetHearingsRequest(String caseRefId, String caseStatus);
}
