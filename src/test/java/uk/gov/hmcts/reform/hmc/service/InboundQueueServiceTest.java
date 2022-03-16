package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    @InjectMocks
    private InboundQueueServiceImpl inboundQueueService;

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        inboundQueueService =
            new InboundQueueServiceImpl(
                OBJECT_MAPPER
            );
    }

    @Nested
    @DisplayName("ProcessInboundMessage")
    class ProcessInboundMessage {
        @Test
        void shouldProcessMessage() throws JsonProcessingException {
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

            inboundQueueService.processMessage(jsonNode);
        }
    }
}
