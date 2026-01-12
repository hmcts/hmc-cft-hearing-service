package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;

public interface HearingStatusAuditService {

    void saveAuditTriageDetailsWithCreatedDate(HearingEntity hearingEntity, String hearingEvent,
                                String httpStatus,String source, String target, JsonNode errorDescription);


    void saveAuditTriageDetailsWithCreatedDate(HearingEntity hearingEntity,
                                       String hearingEvent,String httpStatus, String source, String target,
                                       JsonNode errorDetails, JsonNode otherInfo);

    void saveAuditTriageDetailsWithUpdatedDate(HearingEntity hearingEntity, String hearingEvent,
                                               String httpStatus,String source, String target,
                                               JsonNode errorDescription);

    void saveAuditTriageDetailsWithUpdatedDate(HearingStatusAuditContext hearingStatusAuditContext);

    void saveAuditTriageDetailsForSupportTools(HearingEntity hearingEntity,
                                               String hearingEvent,String httpStatus, String source, String target,
                                               JsonNode errorDetails, JsonNode otherInfo);
}
