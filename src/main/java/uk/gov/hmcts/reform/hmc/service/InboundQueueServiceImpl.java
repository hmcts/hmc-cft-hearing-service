package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.ListAssistResponseException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
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
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl extends HearingIdValidator implements InboundQueueService {

    private final ObjectMapper objectMapper;
    private HearingRepository hearingRepository;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;
    private static final String HEARING_ID = "hearing_id";
    public static final String UNSUPPORTED_HEARING_STATUS = "Hearing has unsupported value for hearing status";
    public static final String MISSING_HEARING_ID = "Message is missing custom header hearing_id";

    public InboundQueueServiceImpl(ObjectMapper objectMapper,
                                   HearingRepository hearingRepository,
                                   HmiHearingResponseMapper hmiHearingResponseMapper,
                                   MessageSenderToTopicConfiguration messageSenderToTopicConfiguration) {
        super(hearingRepository);
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
    }

    @Override
    @Transactional
    public void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type " + messageType + " received");
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            validateResponse(message, messageType, hearingId);
        } else {
            throw new MalformedMessageException(MISSING_HEARING_ID);
        }
    }

    private void validateResponse(JsonNode message, MessageType messageType, Long hearingId)
        throws JsonProcessingException {
        if (messageType.equals(MessageType.HEARING_RESPONSE)) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
            Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
            if (violations.size() == 0) {
                log.info("Successfully converted message to HearingResponseType " + hearingResponse);
                updateHearingAndStatus(hearingId, hearingResponse, messageType, null);
            } else {
                log.info("Total violations found: " + violations.size());
                for (ConstraintViolation<HearingResponse> violation : violations) {
                    log.error("Violations are " + violation.getMessage());
                }
            }
        } else if (messageType.equals(MessageType.ERROR)) {
            ErrorDetails errorResponse = objectMapper.treeToValue(message, ErrorDetails.class);
            log.info("Successfully converted message to ErrorResponse " + errorResponse);
            updateHearingAndStatus(hearingId, null, messageType, errorResponse);
        }
    }

    private void updateHearingAndStatus(Long hearingId, HearingResponse hearingResponse, MessageType messageType,
                                        ErrorDetails errorDetails) {
        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = null;
            if (messageType.equals(MessageType.HEARING_RESPONSE)) {
                hearingToSave = hmiHearingResponseMapper.mapHmiHearingToEntity(
                    hearingResponse,
                    hearingResult.get()
                );
            } else if (messageType.equals(MessageType.ERROR)) {
                hearingToSave = hmiHearingResponseMapper.mapHmiHearingErrorToEntity(
                    errorDetails,
                    hearingResult.get()
                );
            }
            hearingRepository.save(hearingToSave);

            HmcHearingResponse hmcHearingResponse = hmiHearingResponseMapper.mapEntityToHmcModel(
                hearingToSave.getHearingResponses().get((hearingToSave.getHearingResponses().size() - 1)),
                hearingToSave
            );
            messageSenderToTopicConfiguration.sendMessage(hmcHearingResponse.toString());
            if (hmcHearingResponse.getHearingUpdate().getHMCStatus().equals(HearingStatus.EXCEPTION.name())) {
                throw new ListAssistResponseException(
                    hearingId,
                    errorDetails.getErrorCode() + " "
                        + errorDetails.getErrorDescription()
                );

            }
        }
    }
}
