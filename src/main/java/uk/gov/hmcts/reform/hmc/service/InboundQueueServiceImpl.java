package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl implements InboundQueueService {

    private final ObjectMapper objectMapper;
    private HearingRepository hearingRepository;
    private HearingIdValidator hearingIdValidator;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private final ObjectMapperService objectMapperService;
    private static final String HEARING_ID = "hearing_id";
    public static final String UNSUPPORTED_HEARING_STATUS = "Hearing has unsupported value for hearing status";
    public static final String MISSING_HEARING_ID = "Message is missing custom header hearing_id";

    public InboundQueueServiceImpl(ObjectMapper objectMapper,
                                   HearingRepository hearingRepository,
                                   HmiHearingResponseMapper hmiHearingResponseMapper,
                                   MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                   ObjectMapperService objectMapperService,
                                   HearingIdValidator hearingIdValidator) {
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
        this.hearingIdValidator = hearingIdValidator;
    }

    @Override
    public void processMessage(JsonNode message,
                               ServiceBusReceivedMessageContext messageContext)
        throws JsonProcessingException {
        Map<String, Object> applicationProperties = messageContext.getMessage().getApplicationProperties();
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type " + messageType + " received");
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            hearingIdValidator.validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            validateResponse(message, messageType, hearingId, messageContext);
        } else {
            log.error("Error processing message, exception was " + MISSING_HEARING_ID);
        }
    }

    @Override
    public void catchExceptionAndUpdateHearing(Map<String, Object> applicationProperties, Exception exception) {
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            log.error("Error processing message with Hearing id " + hearingId + " exception was "
                          + exception.getMessage());
            Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
            if (hearingResult.isPresent()) {
                HearingEntity hearingEntity = hearingResult.get();
                hearingEntity.setStatus(EXCEPTION.name());
                hearingEntity.setErrorDescription(exception.getMessage());
                hearingRepository.save(hearingEntity);
                log.error("Hearing id: " +  hearingId + " updated to status Exception");
            } else {
                log.error("Hearing id " + hearingId + " not found");
            }
        } else {
            log.error("Error processing message " + MISSING_HEARING_ID);
        }
    }

    private void validateResponse(JsonNode message, MessageType messageType, Long hearingId,
                                  ServiceBusReceivedMessageContext messageContext)
        throws JsonProcessingException {
        if (messageType.equals(MessageType.HEARING_RESPONSE)) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
            Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
            if (violations.isEmpty()) {
                log.info("Successfully converted message to HearingResponseType " + hearingResponse);
                updateHearingAndStatus(hearingId, hearingResponse);
            } else {
                log.info("Total violations found: " + violations.size());
                for (ConstraintViolation<HearingResponse> violation : violations) {
                    log.error("Violations are " + violation.getMessage());
                }
            }
        } else if (messageType.equals(MessageType.LA_SYNC_HEARING_RESPONSE)) {
            SyncResponse syncResponse = objectMapper.treeToValue(message, SyncResponse.class);
            updateHearingAndStatus(hearingId, syncResponse);
        } else if (messageType.equals(MessageType.ERROR)) {
            ErrorDetails errorResponse = objectMapper.treeToValue(message, ErrorDetails.class);
            log.debug("Successfully converted message to ErrorResponse " + errorResponse);
            updateHearingAndStatus(hearingId, errorResponse, messageContext);
        }
    }

    private void updateHearingAndStatus(Long hearingId, ErrorDetails errorDetails,
                                        ServiceBusReceivedMessageContext messageContext) {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiHearingErrorToEntity(
                errorDetails,
                hearingResult.get()
            );
            hearingRepository.save(hearingToSave);
            HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingToSave);
            messageSenderToTopicConfiguration
                .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString());
            if (hmcHearingResponse.getHearingUpdate().getHmcStatus().equals(HearingStatus.EXCEPTION.name())) {
                //Service bus session has to completed first else it will try to re process the message
                messageContext.complete();
                log.error("Hearing id: " +  hearingId + " updated to status Exception");
            }
        }
    }

    @Transactional
    private void updateHearingAndStatus(Long hearingId, HearingResponse hearingResponse) {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = null;
            hearingToSave = hmiHearingResponseMapper.mapHmiHearingToEntity(
                hearingResponse,
                hearingResult.get()
            );
            hearingRepository.save(hearingToSave);
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            if (hearingEntity.isPresent()) {
                HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity.get());
                messageSenderToTopicConfiguration
                    .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString());
            }
        }
    }

    @Transactional
    private void updateHearingAndStatus(Long hearingId, SyncResponse syncResponse) {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiSyncResponseToEntity(
                syncResponse,
                hearingResult.get()
            );
            HearingEntity hearingEntity = hearingRepository.save(hearingToSave);
            HmcHearingResponse hmcHearingResponse = getHmcHearingResponse(hearingEntity);
            messageSenderToTopicConfiguration
                .sendMessage(objectMapperService.convertObjectToJsonNode(hmcHearingResponse).toString());
            if (hearingEntity.getStatus().equals(HearingStatus.EXCEPTION.name())) {
                log.error("Hearing id: " +  hearingId + " updated to status Exception");
            }
        }
    }

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingEntity.getLatestHearingResponse();
        return hearingResponseEntity.isPresent()
            ? hmiHearingResponseMapper.mapEntityToHmcModel(hearingResponseEntity.get(), hearingEntity)
            : hmiHearingResponseMapper.mapEntityToHmcModel(hearingEntity);
    }
}
