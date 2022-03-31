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

import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl implements InboundQueueService {

    private final ObjectMapper objectMapper;

    public InboundQueueServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        MessageType messageType = MessageType.valueOf(applicationProperties.get(MESSAGE_TYPE).toString());
        log.info("Message of type " + messageType + " received");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        if (messageType.equals(MessageType.HEARING_RESPONSE)) {
            validateHearingResponse(message, validator);
        } else if (messageType.equals(MessageType.ERROR)) {
            validateError(message);
        }
    }

    private void validateHearingResponse(JsonNode message, Validator validator) throws JsonProcessingException {
        HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        if (violations.size() == 0) {
            log.info("Successfully converted message to HearingResponseType " + hearingResponse);
        } else {
            log.info("Total violations found: " + violations.size());
            for (ConstraintViolation<HearingResponse> violation : violations) {
                log.error("Violations are " + violation.getMessage());
            }
        }
    }

    private void validateError(JsonNode message) throws JsonProcessingException {
        ErrorDetails errorResponse = objectMapper.treeToValue(message, ErrorDetails.class);
        log.info("Successfully converted message to ErrorResponse " + errorResponse);
    }
}
