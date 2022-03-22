package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.HearingActual;

public interface HearingActualsService {

    void updateHearingActuals(Long hearingId, HearingActual request);
}
