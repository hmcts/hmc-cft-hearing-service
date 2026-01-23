package uk.gov.hmcts.reform.hmc.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDateTime;

@Service
public class HearingStatusAuditServiceImpl implements HearingStatusAuditService {

    private final HearingStatusAuditRepository hearingStatusAuditRepository;

    @Autowired
    public HearingStatusAuditServiceImpl(HearingStatusAuditRepository hearingStatusAuditRepository) {
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;
    }

    @Override
    public void saveAuditTriageDetailsWithCreatedDate(HearingStatusAuditContext auditContext) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetails(auditContext);
        hearingStatusAuditEntity.setStatusUpdateDateTime(auditContext.getHearingEntity().getCreatedDateTime());
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsWithUpdatedDate(HearingStatusAuditContext auditContext) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetails(auditContext);
        hearingStatusAuditEntity.setStatusUpdateDateTime(auditContext.getHearingEntity().getUpdatedDateTime());
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsForSupportTools(HearingStatusAuditContext auditContext) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetails(auditContext);
        hearingStatusAuditEntity.setStatusUpdateDateTime(LocalDateTime.now());
        hearingStatusAuditEntity.setResponseDateTime(null);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(HearingStatusAuditContext auditContext) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetails(auditContext);
        LocalDateTime ts = auditContext.isUseCurrentTimestamp() ? LocalDateTime.now()
            : auditContext.getHearingEntity().getUpdatedDateTime();
        hearingStatusAuditEntity.setStatusUpdateDateTime(ts);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetails(HearingStatusAuditContext hearingStatusAuditContext) {
        HearingStatusAuditEntity hearingStatusAuditEntity = new HearingStatusAuditEntity();
        HearingEntity hearingEntity = hearingStatusAuditContext.getHearingEntity();
        hearingStatusAuditEntity.setHmctsServiceId(hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode());
        hearingStatusAuditEntity.setHearingId(hearingEntity.getId().toString());
        hearingStatusAuditEntity.setStatus(hearingEntity.getStatus());
        hearingStatusAuditEntity.setHearingEvent(hearingStatusAuditContext.getHearingEvent());
        hearingStatusAuditEntity.setHttpStatus(hearingStatusAuditContext.getHttpStatus());
        hearingStatusAuditEntity.setSource(hearingStatusAuditContext.getSource());
        hearingStatusAuditEntity.setTarget(hearingStatusAuditContext.getTarget());
        hearingStatusAuditEntity.setErrorDescription(hearingStatusAuditContext.getErrorDetails());
        hearingStatusAuditEntity.setRequestVersion(hearingEntity.getLatestCaseHearingRequest().getVersionNumber()
                                                       .toString());
        hearingStatusAuditEntity.setResponseDateTime(LocalDateTime.now());
        hearingStatusAuditEntity.setOtherInfo(hearingStatusAuditContext.getOtherInfo());
        return hearingStatusAuditEntity;
    }

    private void saveHearingStatusAudit(HearingStatusAuditEntity hearingStatusAuditEntity) {
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }

}
