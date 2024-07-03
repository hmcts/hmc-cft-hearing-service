package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;

import java.time.LocalDateTime;

public interface PartiesNotifiedService {

    void getPartiesNotified(Long hearingId, Integer requestVersion, LocalDateTime receivedDateTime,
                            PartiesNotified partiesNotified, String clientS2SToken);

    PartiesNotifiedResponses getPartiesNotified(Long hearingId);

}
