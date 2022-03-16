package uk.gov.hmcts.reform.hmc.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.HashMap;
import java.util.Map;


class MessageProcessorIT extends BaseTest {

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEARING_ID = "hearing_id";
    JsonNode jsonNode = OBJECT_MAPPER.readTree("{\n" +
                                                   "  \"meta\": {\n" +
                                                   "    \"transactionIdCaseHQ\": \"<transactionIdCaseHQ>\",\n" +
                                                   "    \"timestamp\": \"2021-08-10T12:20:00\"\n" +
                                                   "  },\n" +
                                                   "  \"hearing\": {\n" +
                                                   "    \"listingRequestId\": \"<listingRequestId>\",\n" +
                                                   "    \"hearingCaseVersionId\": 10,\n" +
                                                   "    \"hearingCaseIdHMCTS\": \"<hearingCaseIdHMCTS>\",\n" +
                                                   "    \"hearingCaseJurisdiction\": {\n" +
                                                   "      \"test\": \"value\"\n" +
                                                   "    },\n" +
                                                   "    \"hearingCaseStatus\": {\n" +
                                                   "      \"code\": \"LISTED\",\n" +
                                                   "      \"description\": \"<description>\"\n" +
                                                   "    },\n" +
                                                   "    \"hearingIdCaseHQ\": \"<hearingIdCaseHQ>\",\n" +
                                                   "    \"hearingType\": {\n" +
                                                   "      \"test\": \"value\"\n" +
                                                   "    },\n" +
                                                   "    \"hearingStatus\": {\n" +
                                                   "      \"code\": \"<code>\",\n" +
                                                   "      \"description\": \"<descrixption>\"\n" +
                                                   "    },\n" +
                                                   "    \"hearingCancellationReason\": \"<hearingCancellationReason>\",\n" +
                                                   "    \"hearingStartTime\": \"2021-08-10T12:20:00\",\n" +
                                                   "    \"hearingEndTime\": \"2021-08-10T12:20:00\",\n" +
                                                   "    \"hearingPrivate\": true,\n" +
                                                   "    \"hearingRisk\": true,\n" +
                                                   "    \"hearingTranslatorRequired\": false,\n" +
                                                   "    \"hearingCreatedDate\": \"2021-08-10T12:20:00\",\n" +
                                                   "    \"hearingCreatedBy\": \"testuser\",\n" +
                                                   "    \"hearingVenue\": {\n" +
                                                   "      \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n" +
                                                   "      \"locationName\": \"<locationName>\",\n" +
                                                   "      \"locationRegion\": \"<locationRegion>\",\n" +
                                                   "      \"locationCluster\": \"<locationCluster>\",\n" +
                                                   "      \"locationReference\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      }\n" +
                                                   "    },\n" +
                                                   "    \"hearingRoom\": {\n" +
                                                   "      \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n" +
                                                   "      \"roomName\": \"<roomName>\",\n" +
                                                   "      \"roomLocationRegion\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      },\n" +
                                                   "      \"roomLocationCluster\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      },\n" +
                                                   "      \"roomLocationReference\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      }\n" +
                                                   "    },\n" +
                                                   "    \"hearingAttendee\": {\n" +
                                                   "      \"entityIdCaseHQ\": \"<id>\",\n" +
                                                   "      \"entityId\": \"<id>\",\n" +
                                                   "      \"entityType\": \"<type>\",\n" +
                                                   "      \"entityClass\": \"<class>\",\n" +
                                                   "      \"entityRole\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      },\n" +
                                                   "      \"hearingChannel\": {\n" +
                                                   "        \"code\": \"<key>\",\n" +
                                                   "        \"description\": \"<value>\"\n" +
                                                   "      }\n" +
                                                   "    },\n" +
                                                   "    \"hearingJoh\": {\n" +
                                                   "      \"johId\": \"<johId>\",\n" +
                                                   "      \"johCode\": \"<johCode>\",\n" +
                                                   "      \"johName\": \"<johName>\",\n" +
                                                   "      \"johPosition\": {\n" +
                                                   "        \"key\": \"<key>\",\n" +
                                                   "        \"value\": \"<value>\"\n" +
                                                   "      },\n" +
                                                   "      \"isPresiding\": false\n" +
                                                   "    },\n" +
                                                   "    \"hearingSession\": {\n" +
                                                   "      \"key\": \"<key>\",\n" +
                                                   "      \"value\": \"<value>\"\n" +
                                                   "    }\n" +
                                                   "  }\n" +
                                                   "}");

    @Autowired
    private InboundQueueService inboundQueueService;

    MessageProcessorIT() throws JsonProcessingException {
    }


    @Test
    void shouldInitiateRequestHearing() {
        //ToDo add to db
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.REQUEST_HEARING);
        applicationProperties.put(HEARING_ID, "2000000000");

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties);
    }

}
