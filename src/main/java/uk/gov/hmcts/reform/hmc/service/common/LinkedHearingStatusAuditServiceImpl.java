package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Component
@Slf4j
public class LinkedHearingStatusAuditServiceImpl implements  LinkedHearingStatusAuditService {

    private final LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;
    private final ObjectMapperService objectMapperService;
    private final HearingRepository hearingRepository;

    @Autowired
    public LinkedHearingStatusAuditServiceImpl(LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository,
                                               ObjectMapperService objectMapperService,
                                               HearingRepository hearingRepository) {
        this.linkedHearingStatusAuditRepository = linkedHearingStatusAuditRepository;
        this.objectMapperService = objectMapperService;
        this.hearingRepository = hearingRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLinkedHearingAuditTriageDetails(String source, LinkedGroupDetails linkedGroupDetails,
                                                    String hearingEvent, String httpStatus, String target,
                                                    JsonNode errorDetails, List<HearingEntity> hearingEntities) {
        List<Long> hearingIds = hearingEntities.stream().map(HearingEntity::getId).toList();
        List<HearingEntity> hearingsWithRequests = hearingRepository.getHearingsWithCaseHearingRequests(hearingIds);
        LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity = mapLinkedHearingStatusAuditDetails(source,
                                                                         linkedGroupDetails, hearingEvent, httpStatus,
                                                                         target, errorDetails, hearingsWithRequests);
        saveLinkedHearingStatusAudit(linkedHearingStatusAuditEntity);
    }


    private LinkedHearingStatusAuditEntity mapLinkedHearingStatusAuditDetails(String source,
                                                                              LinkedGroupDetails linkedGroupDetails,
                                                                              String hearingEvent, String httpStatus,
                                                                              String target, JsonNode errorDetails,
                                                                              List<HearingEntity> hearingEntities) {

        LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity = new LinkedHearingStatusAuditEntity();
        linkedHearingStatusAuditEntity.setHmctsServiceId(hearingEntities.get(0).getLatestCaseHearingRequest()
                                                             .getHmctsServiceCode());
        linkedHearingStatusAuditEntity.setLinkedGroupId(linkedGroupDetails.getLinkedGroupId().toString());
        linkedHearingStatusAuditEntity.setLinkedGroupVersion(linkedGroupDetails.getLinkedGroupLatestVersion()
                                                                 .toString());
        linkedHearingStatusAuditEntity.setLinkedHearingEventDateTime(linkedGroupDetails.getRequestDateTime());
        linkedHearingStatusAuditEntity.setLinkedHearingEvent(hearingEvent);
        linkedHearingStatusAuditEntity.setHttpStatus(httpStatus);
        linkedHearingStatusAuditEntity.setSource(source);
        linkedHearingStatusAuditEntity.setTarget(target);
        linkedHearingStatusAuditEntity.setErrorDescription(errorDetails);
        JsonNode hearingsJson = objectMapperService.convertObjectToJsonNode(getHearingIdInStrings(hearingEntities));
        linkedHearingStatusAuditEntity.setLinkedGroupHearings(hearingsJson);
        return linkedHearingStatusAuditEntity;
    }

    private List<String> getHearingIdInStrings(List<HearingEntity> hearingEntities) {
        List<Long> hearingIdsLong = new ArrayList<>();
        hearingEntities.stream()
            .forEach(hearingEntity -> hearingIdsLong.add(hearingEntity.getId()));
        return hearingIdsLong.stream().map(Object::toString).toList();
    }

    private void saveLinkedHearingStatusAudit(LinkedHearingStatusAuditEntity linkedHearingStatusAuditEntity) {
        linkedHearingStatusAuditRepository.save(linkedHearingStatusAuditEntity);
    }


}
