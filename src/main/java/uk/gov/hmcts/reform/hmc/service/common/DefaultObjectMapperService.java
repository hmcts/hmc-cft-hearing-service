package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    @Override
    public String convertObjectToJsonString(Object object)  {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
