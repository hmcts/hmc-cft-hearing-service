package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ListAssistResponseException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl extends HearingIdValidator implements InboundQueueService {

    private final ObjectMapper objectMapper;
    private HearingRepository hearingRepository;
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
                                   ObjectMapperService objectMapperService) {
        super(hearingRepository);
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
    }

    @Override
    public void processMessage(JsonNode message, Map<String, Object> applicationProperties,
                               ServiceBusReceiverClient client, ServiceBusReceivedMessage serviceBusReceivedMessage)
        throws JsonProcessingException {
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type " + messageType + " received");
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            validateResponse(message, messageType, hearingId, client, serviceBusReceivedMessage);
        } else {
            throw new MalformedMessageException(MISSING_HEARING_ID);
        }
    }

    private void validateResponse(JsonNode message, MessageType messageType, Long hearingId,
                                  ServiceBusReceiverClient client,
                                  ServiceBusReceivedMessage serviceBusReceivedMessage)
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
        } else if (messageType.equals(MessageType.ERROR)) {
            ErrorDetails errorResponse = objectMapper.treeToValue(message, ErrorDetails.class);
            log.debug("Successfully converted message to ErrorResponse " + errorResponse);
            updateHearingAndStatus(hearingId, errorResponse, client, serviceBusReceivedMessage);
        }
    }

    private void updateHearingAndStatus(Long hearingId, ErrorDetails errorDetails,
                                        ServiceBusReceiverClient client,
                                        ServiceBusReceivedMessage message) {
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
                //Service bus sessionhas to completed first else it will try to re process the message
                client.complete(message);
//                throw new ListAssistResponseException(
//                    hearingId,
//                    errorDetails.getErrorCode() + " "
//                        + errorDetails.getErrorDescription()
//                );
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

    private HmcHearingResponse getHmcHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponseEntity =
            hearingEntity.getHearingResponses()
                .stream().max(Comparator.comparing(HearingResponseEntity::getHearingResponseId));
        return hmiHearingResponseMapper
            .mapEntityToHmcModel(hearingResponseEntity
                                     .orElseThrow(() -> new BadRequestException("bad request")), hearingEntity);
    }
}
