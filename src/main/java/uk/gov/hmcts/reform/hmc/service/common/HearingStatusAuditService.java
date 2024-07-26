package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;

import java.time.LocalDateTime;


public interface HearingStatusAuditService {

    void saveAuditTriageDetails(HearingEntity hearingEntity, LocalDateTime statusUpdateDateTime, String hearingEvent,
        String httpStatus,String source, String target, JsonNode errorDescription);

    void saveAuditTriageDetails(HearingEntity hearingEntity, LocalDateTime statusUpdateDateTime,
                                       String hearingEvent,String httpStatus, String source, String target,
                                       JsonNode errorDetails, JsonNode otherInfo);

    HearingStatusAuditEntity getLastAuditTriageDetailsForHearingId(String hearingId);
}
