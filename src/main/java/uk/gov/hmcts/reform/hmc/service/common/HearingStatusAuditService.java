package uk.gov.hmcts.reform.hmc.service.common;

import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;

public interface HearingStatusAuditService {

    void saveHearingStatusAudit(HearingStatusAudit hearingStatusAudit);

    HearingStatusAudit mapHearingStatusAuditDetails(String hmctsServiceId,
                                                    HearingEntity savedEntity, String hearingEvent,
                                                    String source, String target, Object errorDescription,
                                                    String versionNumber);
}
