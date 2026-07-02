package uk.gov.hmcts.reform.hmc.service.common;

import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;

public interface ActualHearingAuditService {

    void saveActualHearingAuditDetails(HearingActual request, ActualHearingEntity actualHearingEntity);

}
