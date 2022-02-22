package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponses;

public interface PartiesNotifiedService {

    PartiesNotifiedResponses getPartiesNotified(Long hearingId);

}