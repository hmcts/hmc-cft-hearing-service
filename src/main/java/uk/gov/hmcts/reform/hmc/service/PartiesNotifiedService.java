package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;

public interface PartiesNotifiedService {

    void getPartiesNotified(Long hearingId, int responseVersions, PartiesNotified partiesNotified);

    PartiesNotifiedResponses getPartiesNotified(Long hearingId);

}
