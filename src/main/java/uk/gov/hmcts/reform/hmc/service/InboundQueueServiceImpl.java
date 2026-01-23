package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EXCEPTION_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FINAL_STATE_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_FINAL_STATE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_ACK;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_RESPONSE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_SUCCESS_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl implements InboundQueueService {

    private final ObjectMapper objectMapper;
    private final HearingRepository hearingRepository;
    private final HearingIdValidator hearingIdValidator;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private final ApplicationParams applicationParams;
    private final HearingStatusAuditService hearingStatusAuditService;
    private static final String HEARING_ID = "hearing_id";
    public static final String UNSUPPORTED_HEARING_STATUS = "Hearing has unsupported value for hearing status";
    public static final String MISSING_HEARING_ID = "Message is missing custom header hearing_id";

    public InboundQueueServiceImpl(ObjectMapper objectMapper,
                                   HearingRepository hearingRepository,
                                   HmiHearingResponseMapper hmiHearingResponseMapper,
                                   MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                   ObjectMapperService objectMapperService,
                                   HearingIdValidator hearingIdValidator,
                                   ApplicationParams applicationParams,
                                   HearingStatusAuditService hearingStatusAuditService) {
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.hearingIdValidator = hearingIdValidator;
        this.applicationParams = applicationParams;
        this.hearingStatusAuditService = hearingStatusAuditService;
    }

    @Override
    public void processMessage(JsonNode message,
                               ServiceBusReceivedMessageContext messageContext)
        throws JsonProcessingException {
        Map<String, Object> applicationProperties = messageContext.getMessage().getApplicationProperties();
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type {} received", messageType);
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            validateResponse(message, messageType, hearingId);
        } else {
            log.error("Error processing message, exception was {}", MISSING_HEARING_ID);
        }
    }

    @Override
    public void catchExceptionAndUpdateHearing(Map<String, Object> applicationProperties, Exception exception) {
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            log.error("Error processing message with Hearing id {} exception was {}",
                      hearingId, exception.getMessage());
            Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
            if (hearingResult.isPresent()) {
                HearingEntity hearingEntity = hearingResult.get();
                hearingEntity.setStatus(EXCEPTION.name());
                hearingEntity.setErrorDescription(exception.getMessage());
                hearingRepository.save(hearingEntity);
                logErrorStatusToException(hearingId, hearingEntity.getLatestCaseReferenceNumber(),
                                          hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode(),
                                          hearingEntity.getErrorDescription());

                JsonNode errorDescription = objectMapper.convertValue(exception.getMessage(), JsonNode.class);
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity)
                        .hearingEvent(LA_RESPONSE)
                        .httpStatus(LA_FAILURE_STATUS)
                        .source(FH)
                        .target(HMC)
                        .errorDetails(errorDescription)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);
            } else {
                log.error("Hearing id {} not found", hearingId);
            }
        } else {
            log.error("Error processing message {}", MISSING_HEARING_ID);
        }
    }

    private void validateResponse(JsonNode message, MessageType messageType, Long hearingId)
        throws JsonProcessingException {
        log.debug("message received for hearing id :{}, messageType: {}, message: {} ",hearingId, messageType,
                  message.toString());
        if (messageType.equals(MessageType.HEARING_RESPONSE)) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
            Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
            if (violations.isEmpty()) {
                log.debug("Successfully converted message to HearingResponseType {}", hearingResponse);
                updateHearingAndStatus(hearingId, hearingResponse);
            } else {
                log.info("Total violations found: {}", violations.size());
                for (ConstraintViolation<HearingResponse> violation : violations) {
                    log.error("Violations are {}", violation.getMessage());
                }
            }
        } else if (messageType.equals(MessageType.LA_SYNC_HEARING_RESPONSE)) {
            SyncResponse syncResponse = objectMapper.treeToValue(message, SyncResponse.class);
            updateHearingAndStatus(hearingId, syncResponse);
        } else if (messageType.equals(MessageType.ERROR)) {
            updateHearingAndStatus(hearingId, message);
        }
    }

    private void updateHearingAndStatus(Long hearingId, JsonNode message) throws JsonProcessingException {
        ErrorDetails errorDetails = objectMapper.treeToValue(message, ErrorDetails.class);
        log.debug("Successfully converted message to ErrorResponse {}", errorDetails);
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiHearingErrorToEntity(
                errorDetails,
                hearingResult.get()
            );
            hearingToSave = hearingToSave.updateLastGoodStatus();
            hearingRepository.save(hearingToSave);
            HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingToSave);
            messageSenderToTopicConfiguration
                .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString(),
                             hmcHearingResponse.getHmctsServiceCode(),hearingId.toString(),
                             getDeploymentIdForHearing(hearingResult.get()));
            if (hmcHearingResponse.getHearingUpdate().getHmcStatus().equals(HearingStatus.EXCEPTION.name())) {
                log.info("Hearing id: {} has response of type : {}", hearingId, MessageType.ERROR);
                logErrorStatusToException(hearingId, hearingToSave.getLatestCaseReferenceNumber(),
                                          hearingToSave.getLatestCaseHearingRequest().getHmctsServiceCode(),
                                          hearingToSave.getErrorDescription());
            }
            HearingStatusAuditContext hearingStatusAuditContext =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingToSave)
                    .hearingEvent(LA_RESPONSE)
                    .httpStatus(LA_FAILURE_STATUS)
                    .source(FH)
                    .target(HMC)
                    .errorDetails(message)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);
        }
    }

    @Transactional
    public void updateHearingAndStatus(Long hearingId, HearingResponse hearingResponse) {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave;
            HearingEntity currentHearing = hearingResult.get();
            HearingStatus currentStatus = HearingStatus.valueOf(currentHearing.getStatus());
            // Terminal statuses: CANCELLED, ADJOURNED, COMPLETED
            if (HearingStatus.isFinalStatus(currentStatus)) {
                log.info(FINAL_STATE_MESSAGE,
                         hearingId,
                         currentHearing.getLatestCaseReferenceNumber(),
                         currentHearing.getLatestCaseHearingRequest().getHmctsServiceCode(),
                         currentStatus,
                         hearingResponse.getHearing().getHearingCaseStatus().getDescription(),
                         HearingCode.getByNumber(hearingResponse.getHearing().getHearingCaseStatus().getCode())
                );
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(currentHearing)
                        .hearingEvent(HEARING_FINAL_STATE)
                        .httpStatus(LA_SUCCESS_STATUS)
                        .source(FH)
                        .target(HMC)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);
                return;
            }
            hearingToSave = hmiHearingResponseMapper.mapHmiHearingToEntity(
                hearingResponse,
                currentHearing
            );
            hearingToSave = hearingToSave.updateLastGoodStatus();
            hearingRepository.save(hearingToSave);
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            if (hearingEntity.isPresent()) {
                HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity.get());
                messageSenderToTopicConfiguration
                    .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString(),
                                 hmcHearingResponse.getHmctsServiceCode(),hearingId.toString(),
                                 getDeploymentIdForHearing(hearingResult.get()));
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity.get())
                        .hearingEvent(LA_RESPONSE)
                        .httpStatus(LA_SUCCESS_STATUS)
                        .source(FH)
                        .target(HMC)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);

            }
        }
    }

    @Transactional
    public void updateHearingAndStatus(Long hearingId, SyncResponse syncResponse) {
        log.debug("{} received for hearing id {} , {}",
                  MessageType.LA_SYNC_HEARING_RESPONSE, hearingId, syncResponse);
        JsonNode errorDescription = null;
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                syncResponse,
                hearingResult.get()
            );
            hearingToSave = hearingToSave.updateLastGoodStatus();
            HearingEntity hearingEntity = hearingRepository.save(hearingToSave);
            HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity);
            messageSenderToTopicConfiguration
                .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString(),
                             hmcHearingResponse.getHmctsServiceCode(),hearingId.toString(),
                             getDeploymentIdForHearing(hearingResult.get()));
            if (hearingEntity.getStatus().equals(HearingStatus.EXCEPTION.name())) {
                errorDescription = objectMapper.convertValue(syncResponse, JsonNode.class);
                log.info("Hearing id: {} has response of type : {}", hearingId, MessageType.LA_SYNC_HEARING_RESPONSE);
                logErrorStatusToException(hearingId, hearingEntity.getLatestCaseReferenceNumber(),
                                          hearingEntity.getLatestCaseHearingRequest().getHmctsServiceCode(),
                                          hearingEntity.getErrorDescription());
            }
            HearingStatusAuditContext hearingStatusAuditContext =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(LA_ACK)
                    .httpStatus(syncResponse.getListAssistHttpStatus().toString())
                    .source(HMC)
                    .target(FH)
                    .errorDetails(errorDescription)
                    .build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);

        }
    }

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingEntity.getLatestHearingResponse();
        return hearingResponseEntity.isPresent()
            ? hmiHearingResponseMapper.mapEntityToHmcModel(hearingResponseEntity.get(), hearingEntity)
            : hmiHearingResponseMapper.mapEntityToHmcModel(hearingEntity);
    }

    private String getDeploymentIdForHearing(HearingEntity hearingEntity) {
        return applicationParams.isHmctsDeploymentIdEnabled() ? hearingEntity.getDeploymentId() : null;
    }

    private void logErrorStatusToException(Long hearingId, String caseRef, String serviceCode,
                                           String errorDescription) {
        log.error(EXCEPTION_MESSAGE, hearingId, caseRef, serviceCode, errorDescription, EXCEPTION.name());
    }

}
