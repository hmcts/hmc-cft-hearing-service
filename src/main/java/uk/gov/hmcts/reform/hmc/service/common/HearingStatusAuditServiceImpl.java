package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDateTime;

@Service
@Component
@Slf4j
public class HearingStatusAuditServiceImpl implements HearingStatusAuditService {

    private final HearingStatusAuditRepository hearingStatusAuditRepository;

    @Autowired
    public HearingStatusAuditServiceImpl(HearingStatusAuditRepository hearingStatusAuditRepository) {
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;

    }

    @Override
    public void saveAuditTriageDetails(HearingEntity hearingEntity, LocalDateTime statusUpdateDateTime,
                                       String hearingEvent,String httpStatus, String source, String target,
                                       JsonNode errorDetails) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetails(hearingEntity,
                                                                                         statusUpdateDateTime,
                                                                                         hearingEvent,httpStatus,
                                                                                         source, target, errorDetails);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetails(HearingEntity hearingEntity,
                                                                   LocalDateTime statusUpdateDateTime,
                                                                   String hearingEvent, String httpStatus,
                                                                   String source, String target,
                                                                   JsonNode errorDetails) {
        HearingStatusAuditEntity hearingStatusAuditEntity = new HearingStatusAuditEntity();
        hearingStatusAuditEntity.setHmctsServiceId(hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode());
        hearingStatusAuditEntity.setHearingId(hearingEntity.getId().toString());
        hearingStatusAuditEntity.setStatus(hearingEntity.getStatus());
        hearingStatusAuditEntity.setStatusUpdateDateTime(statusUpdateDateTime);
        hearingStatusAuditEntity.setHearingEvent(hearingEvent);
        hearingStatusAuditEntity.setHttpStatus(httpStatus);
        hearingStatusAuditEntity.setSource(source);
        hearingStatusAuditEntity.setTarget(target);
        hearingStatusAuditEntity.setErrorDescription(errorDetails);
        hearingStatusAuditEntity.setRequestVersion(hearingEntity.getLatestCaseHearingRequest().getVersionNumber()
                                                       .toString());
        hearingStatusAuditEntity.setResponseDateTime(LocalDateTime.now());
        return hearingStatusAuditEntity;
    }

    private void saveHearingStatusAudit(HearingStatusAuditEntity hearingStatusAuditEntity) {
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }

}
