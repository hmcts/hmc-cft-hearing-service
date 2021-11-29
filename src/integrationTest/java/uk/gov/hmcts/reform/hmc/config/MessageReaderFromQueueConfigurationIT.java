package uk.gov.hmcts.reform.hmc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;

import java.util.HashMap;
import java.util.Map;

class MessageReaderFromQueueConfigurationIT extends BaseTest {


    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();
    private static final JsonNode data = OBJECT_MAPPER.convertValue("Test data", JsonNode.class);
    private static final String TOKEN = "example-token";
    private static final String CASE_LISTING_REQUEST_ID = "testCaseListingRequestId";
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEARING_ID = "hearing_id";

    @MockBean
    private MessageReaderFromQueueConfiguration messageReaderFromQueueConfiguration;

    @Autowired
    private ApplicationParams applicationParams;

    @Test
    void shouldInitiateProcessingOfRequest() {
        Map<String, Object> applicationProperties = new HashMap<>();

        MessageReaderFromQueueConfiguration messageProcessor =
            new MessageReaderFromQueueConfiguration(applicationParams);
        messageProcessor.readMessageFromTopic();
    }

}
