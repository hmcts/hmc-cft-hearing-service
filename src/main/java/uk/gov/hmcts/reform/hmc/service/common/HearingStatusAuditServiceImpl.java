package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.helper.HearingStatusAuditMapper;
import uk.gov.hmcts.reform.hmc.model.HearingStatusAudit;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
public class HearingStatusAuditServiceImpl implements HearingStatusAuditService {

    private final HearingStatusAuditMapper hearingStatusAuditMapper;
    private final HearingStatusAuditRepository hearingStatusAuditRepository;
    private final SecurityUtils securityUtils;


    @Autowired
    public HearingStatusAuditServiceImpl(HearingStatusAuditMapper hearingStatusAuditMapper,
                                         HearingStatusAuditRepository hearingStatusAuditRepository,
                                         SecurityUtils securityUtils) {
        this.hearingStatusAuditMapper = hearingStatusAuditMapper;
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public void saveAuditTriageDetails(String hearingServiceId, String hearingId, String status,
                                       LocalDateTime statusUpdateDateTime, String hearingEvent,
                                       String clientS2SToken, String target, JsonNode errorDescription,
                                       String requestVersion) {
        String source = securityUtils.getServiceNameFromS2SToken(clientS2SToken);
        HearingStatusAudit hearingStatusAudit = mapHearingStatusAuditDetails(hearingServiceId,
             hearingId,  status,  statusUpdateDateTime, hearingEvent, source,  target,
                                                                             errorDescription, requestVersion);
        saveHearingStatusAudit(hearingStatusAudit);

    }


    private HearingStatusAudit mapHearingStatusAuditDetails(String hearingServiceId,
                                                           String hearingId, String status,
                                                            LocalDateTime statusUpdatedTime,
                                                           String hearingEvent,
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
        hearingStatusAudit.setHearingId(hearingId);
        hearingStatusAudit.setStatus(status);
        // TODO need this? can this be set in Entity directly?
        hearingStatusAudit.setStatusUpdateDateTime(statusUpdatedTime);
        // TODo how to retrieve?
        hearingStatusAudit.setHearingEvent(hearingEvent);
        hearingStatusAudit.setHttpStatus("200");
        hearingStatusAudit.setSource(source);
        hearingStatusAudit.setTarget(target);
        // TODo how to retrieve?
        hearingStatusAudit.setErrorDescription(jsonNode);
        hearingStatusAudit.setRequestVersion(versionNumber);
        return  hearingStatusAudit;
    }


    private void saveHearingStatusAudit(HearingStatusAudit hearingStatusAudit) {

        HearingStatusAuditEntity hearingStatusAuditEntity = hearingStatusAuditMapper
            .modelToEntity(hearingStatusAudit);
        hearingStatusAuditRepository.save(hearingStatusAuditEntity);
    }

}
