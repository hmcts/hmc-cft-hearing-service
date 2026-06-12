package uk.gov.hmcts.reform.hmc.service.common;

import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;

public interface ActualHearingAuditService {

    void saveActualHearingAuditDetails(HearingActual request, HearingResponseEntity hearingResponseEntity);

}
