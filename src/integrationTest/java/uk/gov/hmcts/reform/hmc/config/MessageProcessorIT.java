package uk.gov.hmcts.reform.hmc.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ListAssistResponseException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.config.MessageProcessor.MISSING_MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl.MISSING_HEARING_ID;


class MessageProcessorIT extends BaseTest {

    @MockBean
    private MessageSenderToTopicConfiguration messageSenderConfiguration;

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEARING_ID = "hearing_id";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request_hmi.sql";
    JsonNode jsonNode = OBJECT_MAPPER.readTree("{\n"
                                                   + "  \"meta\": {\n"
                                                   + "    \"transactionIdCaseHQ\": \"<transactionIdCaseHQ>\",\n"
                                                   + "    \"timestamp\": \"2021-08-10T12:20:00\"\n"
                                                   + "  },\n"
                                                   + "  \"hearing\": {\n"
                                                   + "    \"listingRequestId\": \"<listingRequestId>\",\n"
                                                   + "    \"hearingCaseVersionId\": 10,\n"
                                                   + "    \"hearingCaseIdHMCTS\": \"<hearingCaseIdHMCTS>\",\n"
                                                   + "    \"hearingCaseJurisdiction\": {\n"
                                                   + "      \"test\": \"value\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingCaseStatus\": {\n"
                                                   + "      \"code\": \"LISTED\",\n"
                                                   + "      \"description\": \"<description>\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingIdCaseHQ\": \"<hearingIdCaseHQ>\",\n"
                                                   + "    \"hearingType\": {\n"
                                                   + "      \"test\": \"value\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingStatus\": {\n"
                                                   + "      \"code\": \"DRAFT\",\n"
                                                   + "      \"description\": \"<descrixption>\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingCancellationReason\""
                                                   + ": \"<hearingCancellationReason>\",\n"
                                                   + "    \"hearingStartTime\": \"2021-08-10T12:20:00\",\n"
                                                   + "    \"hearingEndTime\": \"2021-08-10T12:20:00\",\n"
                                                   + "    \"hearingPrivate\": true,\n"
                                                   + "    \"hearingRisk\": true,\n"
                                                   + "    \"hearingTranslatorRequired\": false,\n"
                                                   + "    \"hearingCreatedDate\": \"2021-08-10T12:20:00\",\n"
                                                   + "    \"hearingCreatedBy\": \"testuser\",\n"
                                                   + "    \"hearingVenue\": {\n"
                                                   + "      \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n"
                                                   + "      \"locationName\": \"<locationName>\",\n"
                                                   + "      \"locationRegion\": \"<locationRegion>\",\n"
                                                   + "      \"locationCluster\": \"<locationCluster>\",\n"
                                                   + "      \"locationReference\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      }\n"
                                                   + "    },\n"
                                                   + "    \"hearingRoom\": {\n"
                                                   + "      \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n"
                                                   + "      \"roomName\": \"<roomName>\",\n"
                                                   + "      \"roomLocationRegion\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      },\n"
                                                   + "      \"roomLocationCluster\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      },\n"
                                                   + "      \"roomLocationReference\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      }\n"
                                                   + "    },\n"
                                                   + "    \"hearingAttendee\": {\n"
                                                   + "      \"entityIdCaseHQ\": \"<id>\",\n"
                                                   + "      \"entityId\": \"<id>\",\n"
                                                   + "      \"entityType\": \"<type>\",\n"
                                                   + "      \"entityClass\": \"<class>\",\n"
                                                   + "      \"entityRole\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      },\n"
                                                   + "      \"hearingChannel\": {\n"
                                                   + "        \"code\": \"<key>\",\n"
                                                   + "        \"description\": \"<value>\"\n"
                                                   + "      }\n"
                                                   + "    },\n"
                                                   + "    \"hearingJoh\": {\n"
                                                   + "      \"johId\": \"<johId>\",\n"
                                                   + "      \"johCode\": \"<johCode>\",\n"
                                                   + "      \"johName\": \"<johName>\",\n"
                                                   + "      \"johPosition\": {\n"
                                                   + "        \"key\": \"<key>\",\n"
                                                   + "        \"value\": \"<value>\"\n"
                                                   + "      },\n"
                                                   + "      \"isPresiding\": false\n"
                                                   + "    },\n"
                                                   + "    \"hearingSession\": {\n"
                                                   + "      \"key\": \"<key>\",\n"
                                                   + "      \"value\": \"<value>\"\n"
                                                   + "    }\n"
                                                   + "  }\n"
                                                   + "}");

    @Autowired
    private InboundQueueService inboundQueueService;

    MessageProcessorIT() throws JsonProcessingException {
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldInitiateRequest() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowErrorForExceptionFlow() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);

        JsonNode errorJsonNode = OBJECT_MAPPER.readTree("{\n"
                                                            + " \"errCode\": 2000,\n"
                                                            + " \"errDesc\": \"unable to create case\"\n"
                                                            + "}");

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        Exception exception = assertThrows(ListAssistResponseException.class, () ->
            messageProcessor.processMessage(errorJsonNode, applicationProperties));
        assertEquals(
            "Error received for hearing Id: 2000000000 with an error message of 2000 unable to create case",
            exception.getMessage()
        );
        ;
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMissingHeaderMessageType() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        Exception exception = assertThrows(MalformedMessageException.class, () ->
            messageProcessor.processMessage(jsonNode, applicationProperties));
        assertEquals(MISSING_MESSAGE_TYPE, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMissingHeaderHearingId() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        Exception exception = assertThrows(MalformedMessageException.class, () ->
            messageProcessor.processMessage(jsonNode, applicationProperties));
        assertEquals(MISSING_HEARING_ID, exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowHearingIdNotFound() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000001");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        Exception exception = assertThrows(HearingNotFoundException.class, () ->
            messageProcessor.processMessage(jsonNode, applicationProperties));
        assertEquals("No hearing found for reference: 2000000001", exception.getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowHearingIdNotMalformed() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "1000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        Exception exception = assertThrows(BadRequestException.class, () ->
            messageProcessor.processMessage(jsonNode, applicationProperties));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }
}
