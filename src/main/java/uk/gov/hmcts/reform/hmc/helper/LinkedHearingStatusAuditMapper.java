package uk.gov.hmcts.reform.hmc.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@Slf4j
public class LinkedHearingStatusAuditMapper {

    private final Clock utcClock;

    public LinkedHearingStatusAuditMapper(Clock utcClock) {
        this.utcClock = utcClock;
    }

    public LinkedHearingStatusAuditEntity modelToEntity(HearingStatusAudit hearingStatusAudit) {
        LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity = new LinkedHearingStatusAuditEntity();
        linkedHearingStatusAuditEntity.setHmctsServiceId(hearingStatusAudit.getHearingServiceId());
        linkedHearingStatusAuditEntity.setLinkedHearingGroupId(hearingStatusAudit.getHearingId());
        linkedHearingStatusAuditEntity.setStatus(hearingStatusAudit.getStatus());
        linkedHearingStatusAuditEntity.setStatusUpdateDateTime(hearingStatusAudit.getStatusUpdateDateTime());
        linkedHearingStatusAuditEntity.setHearingEvent(hearingStatusAudit.getHearingEvent());
        linkedHearingStatusAuditEntity.setHttpStatus(hearingStatusAudit.getHttpStatus());
        linkedHearingStatusAuditEntity.setSource(hearingStatusAudit.getSource());
        linkedHearingStatusAuditEntity.setTarget(hearingStatusAudit.getTarget());
        linkedHearingStatusAuditEntity.setErrorDescription(hearingStatusAudit.getErrorDescription());
        linkedHearingStatusAuditEntity.setRequestVersion(hearingStatusAudit.getRequestVersion());
        linkedHearingStatusAuditEntity.setResponseDateTime(LocalDateTime.now(utcClock));
        return linkedHearingStatusAuditEntity;
    }
}
