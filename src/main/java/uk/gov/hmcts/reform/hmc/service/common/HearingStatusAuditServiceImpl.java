package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.helper.HearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Slf4j
public class HearingStatusAuditServiceImpl implements HearingStatusAuditService {

    private final HearingStatusAuditMapper hearingStatusAuditMapper;
    private final HearingStatusAuditRepository hearingStatusAuditRepository;


    @Autowired
    public HearingStatusAuditServiceImpl(HearingStatusAuditMapper hearingStatusAuditMapper,
                                         HearingStatusAuditRepository hearingStatusAuditRepository) {
        this.hearingStatusAuditMapper = hearingStatusAuditMapper;
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;
    }

    @Override
    public HearingStatusAudit mapHearingStatusAuditDetails(String hearingServiceId,
                                                           HearingEntity savedEntity, String hearingEvent,
                                                           String source, String target, Object errorDescription,
                                                           String versionNumber) {
        JsonNode jsonNode = null;
        try {
            jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HearingStatusAudit hearingStatusAudit = new HearingStatusAudit();
        hearingStatusAudit.setHearingServiceId(hearingServiceId);
        hearingStatusAudit.setHearingId(savedEntity.getId().toString());
        hearingStatusAudit.setStatus(savedEntity.getStatus());
        // TODO need this? can this be set in Entity directly?
        hearingStatusAudit.setStatusUpdateDateTime(LocalDate.EPOCH.atTime(LocalTime.now()));
        // TODo how to retrieve?
        hearingStatusAudit.setHearingEvent(hearingEvent);
        hearingStatusAudit.setHttpStatus("200");
        hearingStatusAudit.setSource(source);
        hearingStatusAudit.setTarget(target);
        // TODo how to retrieve?
        hearingStatusAudit.setErrorDescription(jsonNode);
        hearingStatusAudit.setRequestVersion(versionNumber);
        hearingStatusAudit.setResponseDateTime(LocalDate.EPOCH.atTime(LocalTime.now()));
        return  hearingStatusAudit;
    }

    @Override
    public void saveHearingStatusAudit(HearingStatusAudit hearingStatusAudit) {

        HearingStatusAuditEntity hearingStatusAuditEntity = hearingStatusAuditMapper
            .modelToEntity(hearingStatusAudit);
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }
}
