package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DefaultObjectMapperServiceTest {

    private DefaultObjectMapperService objectMapperService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapperService = new DefaultObjectMapperService(objectMapper);
    }

    @Test
    @DisplayName("should convert object to json node")
    void shouldConvertObjectToJsonNode() {
        Map<String, JsonNode> data = new HashMap<>();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("field", "value");
        data.put("data", node);

        String expectedJson = "{\"data\":{\"field\":\"value\"}}";
        JsonNode jsonNode = objectMapperService.convertObjectToJsonNode(data);

        assertThat(jsonNode.toString(), is(expectedJson));
    }

}
