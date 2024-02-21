package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import java.time.LocalDateTime;


public interface HearingStatusAuditService {

    void saveAuditTriageDetails(HearingEntity hearingEntity, LocalDateTime statusUpdateDateTime, String hearingEvent,
        String httpStatus,String clientS2SToken, String target, JsonNode errorDescription);

}
