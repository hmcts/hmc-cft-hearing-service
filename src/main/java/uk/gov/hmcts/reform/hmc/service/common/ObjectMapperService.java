package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;

public interface ObjectMapperService {

    JsonNode convertObjectToJsonNode(Object object);

}
