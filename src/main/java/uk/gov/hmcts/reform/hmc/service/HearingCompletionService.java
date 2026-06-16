package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingRequestVersionAuditService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
public class HearingCompletionService {
    private final HearingRepository hearingRepository;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private final SecurityUtils securityUtils;
    private final HearingRequestVersionAuditService hearingRequestVersionAuditService;
    private final HearingIdValidator hearingIdValidator;

    public HearingCompletionService(HearingRepository hearingRepository,
                                    HearingStatusAuditService hearingStatusAuditService,
                                    MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                    ObjectMapperService objectMapperService,
                                    HmiHearingResponseMapper hmiHearingResponseMapper,
                                    SecurityUtils securityUtils,
                                    HearingRequestVersionAuditService hearingRequestVersionAuditService,
                                    HearingIdValidator hearingIdValidator) {
        this.hearingRepository = hearingRepository;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.securityUtils = securityUtils;
        this.hearingRequestVersionAuditService = hearingRequestVersionAuditService;
        this.hearingIdValidator = hearingIdValidator;
    }

    @Transactional
    public void completeHearing(Long hearingId, String clientS2SToken, int existingRequestVersion) {

        HearingEntity hearingEntity = updateStatus(hearingId);
        String userId = securityUtils.getUserInfo().getSub();
        JsonNode otherInfo = objectMapperService.convertObjectToJsonNode(Map.of("userId", userId));

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
                         hmcHearingResponse.getHmctsServiceCode(),hearingId.toString(),
                         hearingEntity.getDeploymentId());
    }

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingEntity.getLatestHearingResponse();
        return hearingResponseEntity.isPresent()
            ? hmiHearingResponseMapper.mapEntityToHmcModel(hearingResponseEntity.get(), hearingEntity)
            : hmiHearingResponseMapper.mapEntityToHmcModel(hearingEntity);
    }

    private HearingEntity updateStatus(Long hearingId) {
        ActualHearingEntity actualHearingEntity = hearingIdValidator.getActualHearing(hearingId)
            .orElseThrow(() -> new BadRequestException(HEARING_ACTUALS_MISSING_HEARING_OUTCOME));
        HearingEntity hearingEntity = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        hearingEntity.setStatus(actualHearingEntity.getHearingResultType().getLabel());
        hearingRepository.save(hearingEntity);
        return hearingEntity;
    }


}
