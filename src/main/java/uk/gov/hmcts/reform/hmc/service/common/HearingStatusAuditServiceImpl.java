package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.helper.HearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_TYPE;

@Service
@Component
@Slf4j
public class HearingStatusAuditServiceImpl implements HearingStatusAuditService {

    private final HearingStatusAuditMapper hearingStatusAuditMapper;
    private final HearingStatusAuditRepository hearingStatusAuditRepository;
    private final LinkedHearingStatusAuditMapper linkedHearingStatusAuditMapper;
    private final LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;


    @Autowired
    public HearingStatusAuditServiceImpl(HearingStatusAuditMapper hearingStatusAuditMapper,
                                         HearingStatusAuditRepository hearingStatusAuditRepository,
                                         LinkedHearingStatusAuditMapper linkedHearingStatusAuditMapper,
                                         LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository) {
        this.hearingStatusAuditMapper = hearingStatusAuditMapper;
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;
        this.linkedHearingStatusAuditMapper = linkedHearingStatusAuditMapper;
        this.linkedHearingStatusAuditRepository =  linkedHearingStatusAuditRepository;
    }

    @Override
    public void saveAuditTriageDetails(HearingEntity hearingEntity, LocalDateTime statusUpdateDateTime,
                                       String hearingEvent,String httpStatus, String source, String target,
                                       JsonNode errorDetails, String hearingType) {
        HearingStatusAudit hearingStatusAudit = mapHearingStatusAuditDetails(hearingEntity, statusUpdateDateTime,
                                                                             hearingEvent,httpStatus, source, target,
                                                                             errorDetails);
        if (HEARING_TYPE.equals(hearingType)) {
            saveHearingStatusAudit(hearingStatusAudit);
        } else {
            saveLinkedHearingStatusAudit(hearingStatusAudit);
        }
    }

    private HearingStatusAudit mapHearingStatusAuditDetails(HearingEntity hearingEntity,
                                                            LocalDateTime statusUpdateDateTime,String hearingEvent,
                                                            String httpStatus, String source, String target,
                                                            JsonNode errorDetails) {
        HearingStatusAudit hearingStatusAudit = new HearingStatusAudit();
        hearingStatusAudit.setHearingServiceId(hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode());
        hearingStatusAudit.setHearingId(hearingEntity.getId().toString());
        hearingStatusAudit.setStatus(hearingEntity.getStatus());
        hearingStatusAudit.setStatusUpdateDateTime(statusUpdateDateTime);
        hearingStatusAudit.setHearingEvent(hearingEvent);
        hearingStatusAudit.setHttpStatus(httpStatus);
        hearingStatusAudit.setSource(source);
        hearingStatusAudit.setTarget(target);
        hearingStatusAudit.setErrorDescription(errorDetails);
        hearingStatusAudit.setRequestVersion(hearingEntity.getLatestCaseHearingRequest().getVersionNumber().toString());
        return  hearingStatusAudit;
    }


    private void saveHearingStatusAudit(HearingStatusAudit hearingStatusAudit) {
        HearingStatusAuditEntity hearingStatusAuditEntity = hearingStatusAuditMapper
            .modelToEntity(hearingStatusAudit);
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }

    private void saveLinkedHearingStatusAudit(HearingStatusAudit hearingStatusAudit) {
        LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity = linkedHearingStatusAuditMapper
            .modelToEntity(hearingStatusAudit);
        linkedHearingStatusAuditRepository.save(linkedHearingStatusAuditEntity);
    }

}
