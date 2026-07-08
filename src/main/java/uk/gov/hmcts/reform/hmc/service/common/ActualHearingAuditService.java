package uk.gov.hmcts.reform.hmc.service.common;

import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;

public interface ActualHearingAuditService {

    ActualHearingAuditEntity mapActualHearingAuditDetails(HearingActual request,
                                                          ActualHearingEntity actualHearingEntity);

    void saveActualHearingAuditDetails(ActualHearingAuditEntity actualHearingEntity);

}
