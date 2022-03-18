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
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private static final String HEARING_ID = "hearing_id";
    public static final String UNSUPPORTED_HEARING_STATUS = "Hearing has unsupported value for hearing status";
    public static final String MISSING_HEARING_ID = "Message is missing custom header hearing_id";

    public InboundQueueServiceImpl(ObjectMapper objectMapper, HearingRepository hearingRepository,
                                   HmiHearingResponseMapper hmiHearingResponseMapper) {
        super(hearingRepository);
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
    }

    @Override
    public void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type " + messageType + " received");
        if (messageType.equals(MessageType.HEARING_RESPONSE)) {
            validateHearingResponse(message, applicationProperties);
        } else if (messageType.equals(MessageType.ERROR)) {
            validateError(message, applicationProperties);
        }
    }

    private void updateHearing(HearingResponse hearingResponse, Map<String, Object> applicationProperties) {
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            updateHearingAndStatus(hearingId, hearingResponse);
        } else {
            throw new MalformedMessageException(MISSING_HEARING_ID);
        }

    }

    private void updateHearing(ErrorDetails errorDetails, Map<String, Object> applicationProperties) {
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            updateHearingAndStatus(hearingId, errorDetails);
        } else {
            throw new MalformedMessageException(MISSING_HEARING_ID);
        }

    }

    private void updateHearingAndStatus(Long hearingId, HearingResponse hearingResponse) {

        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiHearingToEntity(
                hearingResponse,
                hearingResult.get()
            );
            hearingRepository.save(hearingToSave);
            // transform and add to queue 79
        }
    }

    private void updateHearingAndStatus(Long hearingId, ErrorDetails errorDetails) {

        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiHearingErrorToEntity(
                errorDetails,
                hearingResult.get()
            );
            hearingRepository.save(hearingToSave);
            // transform and add to queue 79
        }
    }

    private void validateHearingResponse(JsonNode message,
                                         Map<String, Object> applicationProperties) throws JsonProcessingException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        if (violations.size() == 0) {
            log.info("Successfully converted message to HearingResponseType " + hearingResponse);
            updateHearing(hearingResponse, applicationProperties);
        } else {
            log.info("Total violations found: " + violations.size());
            for (ConstraintViolation<HearingResponse> violation : violations) {
                log.error("Violations are " + violation.getMessage());
            }
        }
    }

    private void validateError(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        ErrorDetails errorResponse = objectMapper.treeToValue(message, ErrorDetails.class);
        log.info("Successfully converted message to ErrorResponse " + errorResponse);
        updateHearing(errorResponse, applicationProperties);
    }
}
