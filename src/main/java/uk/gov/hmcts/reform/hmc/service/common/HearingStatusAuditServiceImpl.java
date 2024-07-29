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
import java.util.List;

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
    public void saveAuditTriageDetailsWithCreatedDate(HearingEntity hearingEntity,
                                       String hearingEvent, String httpStatus, String source, String target,
                                       JsonNode errorDetails) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetailsWithCreatedDate(hearingEntity,
                                                                                         hearingEvent,httpStatus,
                                                                                         source, target, errorDetails);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsWithCreatedDate(HearingEntity hearingEntity,
                                                      String hearingEvent,String httpStatus, String source,
                                                      String target, JsonNode errorDetails, JsonNode otherInfo) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetailsWithCreatedDate(hearingEntity,
                                                                                         hearingEvent,httpStatus,
                                                                                         source, target, errorDetails,
                                                                                         otherInfo);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsWithUpdatedDate(HearingEntity hearingEntity,
                                       String hearingEvent,String httpStatus, String source, String target,
                                       JsonNode errorDetails) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetailsWithUpdatedDate(hearingEntity,
                                                                                         hearingEvent,httpStatus,
                                                                                         source, target, errorDetails);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public void saveAuditTriageDetailsWithUpdatedDate(HearingEntity hearingEntity, String hearingEvent,
                                                      String httpStatus, String source, String target,
                                                      JsonNode errorDetails, JsonNode otherInfo) {
        HearingStatusAuditEntity hearingStatusAuditEntity = mapHearingStatusAuditDetailsWithUpdatedDate(hearingEntity,
                                                                                         hearingEvent,httpStatus,
                                                                                         source, target, errorDetails,
                                                                                         otherInfo);
        saveHearingStatusAudit(hearingStatusAuditEntity);
    }

    @Override
    public HearingStatusAuditEntity getLastAuditTriageDetailsForHearingId(String hearingId) {
        List<HearingStatusAuditEntity> hearingStatusAuditEntityList =
            hearingStatusAuditRepository.findByHearingId(hearingId);
        if (null != hearingStatusAuditEntityList && !hearingStatusAuditEntityList.isEmpty()) {
            return hearingStatusAuditEntityList.get(hearingStatusAuditEntityList.size());
        }
        return null;
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetailsWithCreatedDate(HearingEntity hearingEntity,
                                                                  String hearingEvent, String httpStatus,
                                                                  String source, String target,
                                                                  JsonNode errorDetails) {
        return mapHearingStatusAuditDetailsWithCreatedDate(hearingEntity, hearingEvent, httpStatus, source,
                                            target, errorDetails, null);
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetailsWithCreatedDate(HearingEntity hearingEntity,
                                                                                 String hearingEvent, String httpStatus,
                                                                                 String source, String target,
                                                                                 JsonNode errorDetails,
                                                                                 JsonNode otherInfo) {
        HearingStatusAuditEntity hearingStatusAuditEntity = generateHearingStatusAuditEntity(hearingEntity,
                                                                                             hearingEvent, httpStatus,
                                                                                             source, target,
                                                                                             errorDetails, otherInfo);
        hearingStatusAuditEntity.setStatusUpdateDateTime(hearingEntity.getCreatedDateTime());
        return hearingStatusAuditEntity;
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetailsWithUpdatedDate(HearingEntity hearingEntity,
                                                                   String hearingEvent, String httpStatus,
                                                                   String source, String target,
                                                                   JsonNode errorDetails) {
        return mapHearingStatusAuditDetailsWithUpdatedDate(hearingEntity, hearingEvent, httpStatus, source,
                                            target, errorDetails, null);
    }

    private HearingStatusAuditEntity mapHearingStatusAuditDetailsWithUpdatedDate(HearingEntity hearingEntity,
                                                                                 String hearingEvent, String httpStatus,
                                                                                 String source, String target,
                                                                                 JsonNode errorDetails,
                                                                                 JsonNode otherInfo) {
        HearingStatusAuditEntity hearingStatusAuditEntity = generateHearingStatusAuditEntity(hearingEntity,
                                                                                             hearingEvent, httpStatus,
                                                                                             source, target,
                                                                                             errorDetails, otherInfo);
        hearingStatusAuditEntity.setStatusUpdateDateTime(hearingEntity.getUpdatedDateTime());
        return hearingStatusAuditEntity;
    }

    private HearingStatusAuditEntity generateHearingStatusAuditEntity(HearingEntity hearingEntity,
                                                                      String hearingEvent, String httpStatus,
                                                                      String source, String target,
                                                                      JsonNode errorDetails, JsonNode otherInfo) {
        HearingStatusAuditEntity hearingStatusAuditEntity = new HearingStatusAuditEntity();
        hearingStatusAuditEntity.setHmctsServiceId(hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode());
        hearingStatusAuditEntity.setHearingId(hearingEntity.getId().toString());
        hearingStatusAuditEntity.setStatus(hearingEntity.getStatus());
        hearingStatusAuditEntity.setHearingEvent(hearingEvent);
        hearingStatusAuditEntity.setHttpStatus(httpStatus);
        hearingStatusAuditEntity.setSource(source);
        hearingStatusAuditEntity.setTarget(target);
        hearingStatusAuditEntity.setErrorDescription(errorDetails);
        hearingStatusAuditEntity.setRequestVersion(hearingEntity.getLatestCaseHearingRequest().getVersionNumber()
                                                       .toString());
        hearingStatusAuditEntity.setResponseDateTime(LocalDateTime.now());
        hearingStatusAuditEntity.setOtherInfo(otherInfo);
        return hearingStatusAuditEntity;
    }

    private void saveHearingStatusAudit(HearingStatusAuditEntity hearingStatusAuditEntity) {
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }

}
