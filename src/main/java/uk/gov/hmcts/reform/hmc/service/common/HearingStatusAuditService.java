package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public interface HearingStatusAuditService {

    void saveAuditTriageDetails(String serviceCode, String hearingId, String status,
                           LocalDateTime statusUpdateDateTime, String hearingEvent, String clientS2SToken,
                           String target, JsonNode errorDescription, String requestVersion);

}
