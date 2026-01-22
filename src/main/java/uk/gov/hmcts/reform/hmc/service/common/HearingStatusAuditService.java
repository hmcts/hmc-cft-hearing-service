package uk.gov.hmcts.reform.hmc.service.common;

import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;

public interface HearingStatusAuditService {

    void saveAuditTriageDetailsWithCreatedDate(HearingStatusAuditContext hearingStatusAuditContext);

    void saveAuditTriageDetailsForSupportTools(HearingStatusAuditContext hearingStatusAuditContext);

    void saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(HearingStatusAuditContext hearingStatusAuditContext);

    void saveAuditTriageDetailsWithUpdatedDate(HearingStatusAuditContext hearingStatusAuditContext);

}
