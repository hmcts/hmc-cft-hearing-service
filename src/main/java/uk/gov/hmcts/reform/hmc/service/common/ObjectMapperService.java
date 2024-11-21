package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface ObjectMapperService {

    JsonNode convertObjectToJsonNode(Object object);

    String convertObjectToJsonString(Object object);

}
