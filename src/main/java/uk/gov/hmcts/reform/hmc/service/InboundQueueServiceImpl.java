package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl implements InboundQueueService {

    private final ObjectMapper objectMapper;

    public InboundQueueServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void processMessage(JsonNode message) throws JsonProcessingException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
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
}
