package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingAuditRepository;

import java.time.LocalDateTime;

@Service
public class ActualHearingAuditServiceImpl implements ActualHearingAuditService {

    private ActualHearingAuditRepository actualHearingAuditRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ActualHearingAuditServiceImpl(ActualHearingAuditRepository actualHearingAuditRepository,
                                         ObjectMapper objectMapper) {
        this.actualHearingAuditRepository = actualHearingAuditRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveActualHearingAuditDetails(HearingActual request, HearingResponseEntity hearingResponseEntity) {
        ActualHearingAuditEntity entity = mapActualHearingAuditDetails(request, hearingResponseEntity);
        saveActualHearingAudit(entity);
    }

    private ActualHearingAuditEntity mapActualHearingAuditDetails(HearingActual request,
                                                                  HearingResponseEntity hearingResponseEntity) {
        ActualHearingAuditEntity actualHearingAuditEntity = new ActualHearingAuditEntity();
        actualHearingAuditEntity.setHearingResponseId(hearingResponseEntity.getHearingResponseId());
        actualHearingAuditEntity.setHearingId(hearingResponseEntity.getHearing().getId());
        actualHearingAuditEntity.setAuditCreateDateTime(LocalDateTime.now());
        actualHearingAuditEntity.setActualHearingAuditRecord(objectMapper.convertValue(request, JsonNode.class));
        return actualHearingAuditEntity;
    }

    private void saveActualHearingAudit(ActualHearingAuditEntity actualHearingAuditEntity) {
        actualHearingAuditRepository.save(actualHearingAuditEntity);
    }

}
