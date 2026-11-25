package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.service.common.HearingRequestVersionAuditService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_ACTUALS_COMPLETION;

@Service
public class HearingCompletionService {
    private final HearingStatusAuditService hearingStatusAuditService;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private final SecurityUtils securityUtils;
    private final HearingRequestVersionAuditService hearingRequestVersionAuditService;

    public HearingCompletionService(HearingStatusAuditService hearingStatusAuditService,
                                    MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                    ObjectMapperService objectMapperService,
                                    HmiHearingResponseMapper hmiHearingResponseMapper,
                                    SecurityUtils securityUtils,
                                    HearingRequestVersionAuditService hearingRequestVersionAuditService) {
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.securityUtils = securityUtils;
        this.hearingRequestVersionAuditService = hearingRequestVersionAuditService;
    }

    @Transactional
    public void completeHearing(HearingEntity hearingEntity, String clientS2SToken, int existingRequestVersion) {
        String userId = Optional.ofNullable(securityUtils.getUserInfo())
            .map(u -> u.getSub()).orElse(null);
        Map<String, String> otherInfoMap = new HashMap<>();
        if (userId != null) {
            otherInfoMap.put("userId", userId);
        }
        JsonNode otherInfo = objectMapperService.convertObjectToJsonNode(otherInfoMap);

        hearingRequestVersionAuditService.auditChangeInRequestVersion(hearingEntity, existingRequestVersion,
                                                                      clientS2SToken, true);
        HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity);
        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(POST_HEARING_ACTUALS_COMPLETION)
                .httpStatus(String.valueOf(HttpStatus.OK.value()))
                .source(clientS2SToken)
                .target(HMC)
                .otherInfo(otherInfo)
                .useCurrentTimestamp(true)
                .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
        messageSenderToTopicConfiguration
            .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString(),
                         hmcHearingResponse.getHmctsServiceCode(),hearingEntity.getId().toString(),
                         hearingEntity.getDeploymentId());
    }

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingEntity.getLatestHearingResponse();
        return hearingResponseEntity.isPresent()
            ? hmiHearingResponseMapper.mapEntityToHmcModel(hearingResponseEntity.get(), hearingEntity)
            : hmiHearingResponseMapper.mapEntityToHmcModel(hearingEntity);
    }

}
