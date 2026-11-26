package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingAuditRepository;

import java.time.LocalDateTime;

@Service
public class ActualHearingAuditServiceImpl implements ActualHearingAuditService {

    private ActualHearingAuditRepository actualHearingAuditRepository;
    private final ObjectMapper objectMapper;

    public ActualHearingAuditServiceImpl(ActualHearingAuditRepository actualHearingAuditRepository,
                                         ObjectMapper objectMapper) {
        this.actualHearingAuditRepository = actualHearingAuditRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveActualHearingAuditDetails(HearingActual request, ActualHearingEntity actualHearingEntity) {

        ActualHearingAuditEntity entity = mapActualHearingAuditDetails(request, actualHearingEntity);
        saveActualHearingAudit(entity);

    }

    private ActualHearingAuditEntity mapActualHearingAuditDetails(HearingActual request,
                                                                  ActualHearingEntity actualHearingEntity) {
        ActualHearingAuditEntity actualHearingAuditEntity = new ActualHearingAuditEntity();
        HearingResponseEntity hearingResponseEntity = actualHearingEntity.getHearingResponse();
        actualHearingAuditEntity.setHearingId(hearingResponseEntity.getHearing().getId());
        actualHearingAuditEntity.setHearingResponseId(hearingResponseEntity.getHearingResponseId());
        actualHearingAuditEntity.setActualHearingAuditRecord(objectMapper.convertValue(request, JsonNode.class));
        actualHearingAuditEntity.setAuditCreateDateTime(LocalDateTime.now());
        return actualHearingAuditEntity;
    }

    private void saveActualHearingAudit(ActualHearingAuditEntity actualHearingAuditEntity) {
        actualHearingAuditRepository.save(actualHearingAuditEntity);
    }

}
