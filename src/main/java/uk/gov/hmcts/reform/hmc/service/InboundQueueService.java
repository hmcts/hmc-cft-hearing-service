package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;

import java.util.Map;

public interface InboundQueueService {

    void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException;

    HearingStatus getHearingStatus(HearingResponse hearing, HearingEntity hearingEntity);
}
