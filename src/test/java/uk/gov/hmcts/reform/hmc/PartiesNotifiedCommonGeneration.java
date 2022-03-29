package uk.gov.hmcts.reform.hmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PartiesNotifiedCommonGeneration {

    protected static final Logger logger = LoggerFactory.getLogger(PartiesNotifiedCommonGeneration.class);

    protected List<PartiesNotifiedResponse> generateResponses() {
        PartiesNotifiedResponse response1 = generateResponse(1L);
        response1.setPartiesNotified(LocalDateTime.now().minusDays(6));
        PartiesNotifiedResponse response2 = generateResponse(2L);
        response2.setPartiesNotified(LocalDateTime.now().minusDays(4));
        PartiesNotifiedResponse response3 = generateResponse(3L);
        response3.setPartiesNotified(LocalDateTime.now().minusDays(3));

        List<PartiesNotifiedResponse> responses = new ArrayList<>();
        responses.add(response1);
        responses.add(response2);
        responses.add(response3);
        logger.info("List<PartiesNotifiedResponse>: {}", responses);
        return responses;
    }

    protected PartiesNotifiedResponse generateResponse(Long hearingResponseId) {
        PartiesNotifiedResponse response = new PartiesNotifiedResponse();
        response.setResponseVersion("2");
        response.setResponseReceivedDateTime(LocalDateTime.now().minusDays(hearingResponseId));
        response.setRequestVersion("1");
        response.setPartiesNotified(LocalDateTime.now().plusDays(2).plusHours(2));
        logger.info("partiesNotifiedResponse: {}", response);
        return response;
    }

    protected List<HearingResponseEntity> generateEntities(Long hearingId) {
        HearingResponseEntity entity1 = generateResponseEntity(1L);
        entity1.setPartiesNotifiedDateTime(LocalDateTime.now().minusDays(6));
        HearingResponseEntity entity2 = generateResponseEntity(1L);
        entity2.setPartiesNotifiedDateTime(LocalDateTime.now().minusDays(4));
        HearingResponseEntity entity3 = generateResponseEntity(1L);

        HearingEntity hearingEntity = generateHearingEntity(hearingId);
        entity1.setHearing(hearingEntity);
        entity2.setHearing(hearingEntity);
        entity3.setHearing(hearingEntity);

        List<HearingResponseEntity> entities = new ArrayList<>();
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);

        logger.info("List<HearingResponseEntity>: {}", entities);
        return entities;
    }

    protected HearingResponseEntity generateResponseEntity(Long hearingResponseId) {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setHearingResponseId(hearingResponseId);
        entity.setResponseVersion("2");
        entity.setRequestTimeStamp(LocalDateTime.now().minusDays(hearingResponseId));
        entity.setRequestVersion("1");
        logger.info("hearingResponseEntity: {}", entity);
        return entity;
    }

    protected HearingEntity generateHearingEntity(Long hearingId) {
        HearingEntity entity = new HearingEntity();
        entity.setId(hearingId);
        logger.info("hearingEntity: {}", entity);
        return entity;
    }


}
