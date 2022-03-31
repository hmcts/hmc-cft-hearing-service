package uk.gov.hmcts.reform.hmc.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;

public interface HearingActualsService {

    ResponseEntity<HearingActualResponse> getHearingActuals(Long hearingId);

}
