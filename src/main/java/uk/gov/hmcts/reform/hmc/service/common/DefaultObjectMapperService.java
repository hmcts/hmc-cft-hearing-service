package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class DefaultObjectMapperService implements ObjectMapperService {

    private final ObjectMapper objectMapper;

    public DefaultObjectMapperService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode convertObjectToJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }

}
