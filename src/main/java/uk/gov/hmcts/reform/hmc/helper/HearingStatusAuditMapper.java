package uk.gov.hmcts.reform.hmc.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@Slf4j
public class HearingStatusAuditMapper {

    private final Clock utcClock;

    public HearingStatusAuditMapper(Clock utcClock) {
        this.utcClock = utcClock;
    }

    public HearingStatusAuditEntity modelToEntity(HearingStatusAudit hearingStatusAudit) {

        HearingStatusAuditEntity hearingStatusAuditEntity = new HearingStatusAuditEntity();
        hearingStatusAuditEntity.setHmctsServiceId(hearingStatusAudit.getHearingServiceId());
        hearingStatusAuditEntity.setHearingId(hearingStatusAudit.getHearingId());
        hearingStatusAuditEntity.setStatus(hearingStatusAudit.getStatus());
        hearingStatusAuditEntity.setStatusUpdateDateTime(LocalDateTime.now(utcClock));
        hearingStatusAuditEntity.setHearingEvent(hearingStatusAudit.getHearingEvent());
        hearingStatusAuditEntity.setHttpStatus(hearingStatusAudit.getHttpStatus());
        hearingStatusAuditEntity.setSource(hearingStatusAudit.getSource());
        hearingStatusAuditEntity.setTarget(hearingStatusAudit.getTarget());
        hearingStatusAuditEntity.setErrorDescription(hearingStatusAudit.getErrorDescription());
        hearingStatusAuditEntity.setRequestVersion(hearingStatusAudit.getRequestVersion());
        hearingStatusAuditEntity.setResponseDateTime(LocalDateTime.now(utcClock));
        return hearingStatusAuditEntity;
    }
}
