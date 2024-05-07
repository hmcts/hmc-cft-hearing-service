package uk.gov.hmcts.reform.hmc.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;

import java.time.Clock;

@Component
@Slf4j
public class LinkedHearingStatusAuditMapper {

    private final Clock utcClock;

    public LinkedHearingStatusAuditMapper(Clock utcClock) {
        this.utcClock = utcClock;
    }

    public LinkedHearingStatusAuditEntity modelToEntity(HearingStatusAudit hearingStatusAudit)
        throws JsonProcessingException {
        JsonNode jsonObject = new ObjectMapper().readTree("{\"TO DO\":"
                                                              + " \"TO DO \"}");
        LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity = new LinkedHearingStatusAuditEntity();
        linkedHearingStatusAuditEntity.setHmctsServiceId(hearingStatusAudit.getHearingServiceId());
        linkedHearingStatusAuditEntity.setLinkedGroupId(hearingStatusAudit.getHearingId());
        linkedHearingStatusAuditEntity.setLinkedGroupVersion(hearingStatusAudit.getStatus());
        linkedHearingStatusAuditEntity.setLinkedHearingEventDateTime(hearingStatusAudit.getStatusUpdateDateTime());
        linkedHearingStatusAuditEntity.setLinkedHearingEvent(hearingStatusAudit.getHearingEvent());
        linkedHearingStatusAuditEntity.setHttpStatus(hearingStatusAudit.getHttpStatus());
        linkedHearingStatusAuditEntity.setSource(hearingStatusAudit.getSource());
        linkedHearingStatusAuditEntity.setTarget(hearingStatusAudit.getTarget());
        linkedHearingStatusAuditEntity.setErrorDescription(hearingStatusAudit.getErrorDescription());
        linkedHearingStatusAuditEntity.setOtherInfo(jsonObject);
        linkedHearingStatusAuditEntity.setLinkedGroupHearings(jsonObject);
        return linkedHearingStatusAuditEntity;
    }
}
