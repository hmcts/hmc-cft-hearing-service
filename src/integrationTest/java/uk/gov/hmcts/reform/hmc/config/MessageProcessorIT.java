package uk.gov.hmcts.reform.hmc.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.repository.HearingDayDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;
import uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import static java.time.LocalDateTime.parse;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageProcessorIT extends BaseTest {

    @Inject
    private HearingDayDetailsRepository hearingDayDetailsRepository;

    @MockBean
    private MessageSenderToTopicConfiguration messageSenderConfiguration;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private ServiceBusReceiverClient client;


    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module())
        .build();
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEARING_ID = "hearing_id";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request_hmi.sql";
    private static final String HEARING = "{\n"
            + "  \"meta\": {\n"
           + "    \"transactionIdCaseHQ\": \"<transactionIdCaseHQ>\",\n"
           + "    \"timestamp\": \"2021-08-10T12:20:00\"\n"
           + "  },\n"
           + "  \"hearing\": {\n"
           + "    \"listingRequestId\": \"<listingRequestId>\",\n"
           + "    \"hearingCaseVersionId\": %s,\n"
           + "    \"hearingCaseIdHMCTS\": \"<hearingCaseIdHMCTS>\",\n"
           + "    \"hearingCaseJurisdiction\": {\n"
           + "      \"test\": \"value\"\n"
           + "    },\n"
           + "    \"hearingCaseStatus\": {\n"
           + "      \"code\": \"100\",\n"
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
           + "      \"locationReferences\": [{\n"
           + "        \"key\": \"EPIMS\",\n"
           + "        \"value\": \"<value>\"\n"
           + "      }]\n"
           + "    },\n"
           + "    \"hearingRoom\": {\n"
           + "      \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n"
           + "      \"locationName\": \"<roomName>\",\n"
           + "      \"locationRegion\": {\n"
           + "        \"key\": \"<key>\",\n"
           + "        \"value\": \"<value>\"\n"
           + "      },\n"
           + "      \"locationCluster\": {\n"
           + "        \"key\": \"<key>\",\n"
           + "        \"value\": \"<value>\"\n"
           + "      },\n"
           + "      \"locationReferences\": {\n"
           + "        \"key\": \"<key>\",\n"
           + "        \"value\": \"<value>\"\n"
           + "      }\n"
           + "    },\n"
           + "    \"hearingAttendees\": [{\n"
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
           + "    }],\n"
           + "    \"hearingJohs\": [{\n"
           + "      \"johId\": \"<johId>\",\n"
           + "      \"johCode\": \"<johCode>\",\n"
           + "      \"johName\": \"<johName>\",\n"
           + "      \"johPosition\": {\n"
           + "        \"key\": \"<key>\",\n"
           + "        \"value\": \"<value>\"\n"
           + "      },\n"
           + "      \"isPresiding\": false\n"
           + "    }],\n"
           + "    \"hearingSessions\": %s\n"
           + "  }\n"
           + "}";

    JsonNode jsonNode = OBJECT_MAPPER.readTree(String.format(HEARING, 1, "[\n]"));

    JsonNode jsonMisMatchOnRequestVersion = OBJECT_MAPPER.readTree(String.format(HEARING, 10,
            createHearingSessions(List.of("2021-08-10T12:20:00"), List.of("2021-08-10T12:20:00"))));

    @Autowired
    private InboundQueueService inboundQueueService;

    MessageProcessorIT() throws JsonProcessingException {
    }

    private String createHearingSessions(List<String> startTimes, List<String> endTimes) {
        assertEquals(startTimes.size(), endTimes.size());
        return "[\n" + IntStream.range(0, startTimes.size()).mapToObj(i ->
                createHearingSession(startTimes.get(i), endTimes.get(i))
        ).collect(Collectors.joining(",")) + "]";
    }

    private String createHearingSession(String startTime, String endTime) {
        return String.format("{\n"
                        + " \"hearingStartTime\": \"%s\",\n"
                        + " \"hearingEndTime\": \"%s\",\n"
                        + " \"hearingVenue\": {\n"
                        + "     \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n"
                        + "     \"locationName\": \"<locationName>\",\n"
                        + "     \"locationRegion\": \"<locationRegion>\",\n"
                        + "     \"locationCluster\": \"<locationCluster>\",\n"
                        + "     \"locationReferences\": [{\n"
                        + "         \"key\": \"EPIMS\",\n"
                        + "         \"value\": \"<value>\"\n"
                        + "     }]\n"
                        + " },\n"
                        + " \"hearingRoom\": {\n"
                        + "     \"locationIdCaseHQ\": \"<locationIdCaseHQ>\",\n"
                        + "     \"locationName\": \"<roomName>\",\n"
                        + "     \"locationRegion\": {\n"
                        + "     \"key\": \"<key>\",\n"
                        + "         \"value\": \"<value>\"\n"
                        + "     },\n"
                        + "     \"locationCluster\": {\n"
                        + "         \"key\": \"<key>\",\n"
                        + "         \"value\": \"<value>\"\n"
                        + "     },\n"
                        + "     \"locationReferences\": {\n"
                        + "         \"key\": \"<key>\",\n"
                        + "         \"value\": \"<value>\"\n"
                        + "     }\n"
                        + " }\n"
                        + "}",
                startTime,
                endTime);
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldInitiateRequest() {
        initiateRequest(jsonNode);
    }

    private void initiateRequest(JsonNode jsonNode) {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.INFO, logsList.get(1).getLevel());
        assertEquals("Message of type HEARING_RESPONSE received", logsList.get(0).getMessage());
        assertTrue(logsList.get(1).getMessage().contains("Successfully converted message to HearingResponseType"));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowErrorForExceptionFlow() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        JsonNode errorJsonNode = OBJECT_MAPPER.readTree("{\n"
                                                            + " \"errCode\": 2000,\n"
                                                            + " \"errDesc\": \"unable to create case\"\n"
                                                            + "}");
        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(errorJsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Message of type ERROR received", logsList.get(0).getMessage());
        assertEquals(Level.ERROR, logsList.get(1).getLevel());
        assertEquals("Hearing id: 2000000000 updated to status Exception", logsList.get(1).getMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(0, logsListMessageProcessor.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMisMatchOnRequestVersion() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonMisMatchOnRequestVersion, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(4, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.INFO, logsList.get(1).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals(Level.ERROR, logsList.get(3).getLevel());
        assertEquals("Message of type HEARING_RESPONSE received", logsList.get(0).getMessage());
        assertTrue(logsList.get(1).getMessage().contains("Successfully converted message to HearingResponseType"));
        assertEquals("Error processing message with Hearing id 2000000000 exception was "
                         + "Cannot find request version 10 for hearing 2000000000", logsList.get(2).getMessage());
        assertEquals("Hearing id: 2000000000 updated to status Exception", logsList.get(3).getMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(2, logsListMessageProcessor.size());
        assertEquals(Level.ERROR, logsListMessageProcessor.get(0).getLevel());
        assertEquals("Error for message with id null with error "
                         + "Cannot find request version 10 for hearing 2000000000",
                     logsListMessageProcessor.get(0).getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMissingHeaderMessageType() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(0, logsList.size());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(1, logsListMessageProcessor.size());
        assertEquals(Level.ERROR, logsListMessageProcessor.get(0).getLevel());
        assertEquals("Message is missing custom header message_type for message with message with id null",
                     logsListMessageProcessor.get(0).getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMissingHeaderHearingId() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.ERROR, logsList.get(1).getLevel());
        assertEquals("Message of type HEARING_RESPONSE received", logsList.get(0).getMessage());
        assertEquals("Error processing message, exception was Message is missing custom header hearing_id",
                     logsList.get(1).getMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(0, logsListMessageProcessor.size());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowHearingIdNotFound() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000001");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Message of type HEARING_RESPONSE received", logsList.get(0).getMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(1, logsListMessageProcessor.size());
        assertEquals(Level.ERROR, logsListMessageProcessor.get(0).getLevel());
        assertEquals("Error for message with id null with error No hearing found for reference: 2000000001",
                     logsListMessageProcessor.get(0).getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowHearingIdNotMalformed() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "1000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger loggerMessageProcessor = (Logger) LoggerFactory.getLogger(MessageProcessor.class);
        ListAppender<ILoggingEvent> listAppenderMessageProcessor = new ListAppender<>();
        listAppenderMessageProcessor.start();
        loggerMessageProcessor.addAppender(listAppenderMessageProcessor);

        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        messageProcessor.processMessage(jsonNode, applicationProperties, client, message);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(3, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals("Message of type HEARING_RESPONSE received", logsList.get(0).getMessage());
        assertEquals("Error processing message with Hearing id 1000000000 exception was "
                         + "Invalid hearing Id", logsList.get(1).getMessage());
        assertEquals("Hearing id 1000000000 not found", logsList.get(2).getMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(2, logsListMessageProcessor.size());
        assertEquals(Level.ERROR, logsListMessageProcessor.get(0).getLevel());
        assertEquals("Error for message with id null with error Invalid hearing Id",
                     logsListMessageProcessor.get(0).getMessage());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldInitiateRequest_shouldStoreSingleHearingSessionForDay() throws JsonProcessingException {

        JsonNode hearingSessionsJsonNode = OBJECT_MAPPER.readTree(String.format(HEARING, 1,
                createHearingSessions(
                        List.of("2022-02-10T10:30:00", "2022-02-10T12:00:00", "2022-02-10T14:30:00"),
                        List.of("2022-02-10T11:30:00", "2022-02-10T12:30:00", "2022-02-10T16:30:00"))
        ));

        initiateRequest(hearingSessionsJsonNode);

        final Iterable<HearingDayDetailsEntity> hearingDayDetailsEntities = hearingDayDetailsRepository.findAll();

        assertEquals(1, hearingDayDetailsEntities.spliterator().estimateSize());
        final HearingDayDetailsEntity hearingDayDetailsEntity = hearingDayDetailsEntities.iterator().next();

        assertEquals(parse("2022-02-10T10:30:00"), hearingDayDetailsEntity.getStartDateTime());
        assertEquals(parse("2022-02-10T16:30:00"), hearingDayDetailsEntity.getEndDateTime());
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldInitiateRequest_shouldStoreSingleHearingSessionPerDay() throws JsonProcessingException {
        JsonNode hearingSessionsJsonNode = OBJECT_MAPPER.readTree(String.format(HEARING, 1,
                createHearingSessions(
                        List.of("2022-02-10T10:30:00", "2022-02-11T12:00:00"),
                        List.of("2022-02-10T11:30:00", "2022-02-11T12:30:00"))
        ));

        final var februaryTenth =
                new ImmutablePair<>(parse("2022-02-10T10:30:00"), parse("2022-02-10T11:30:00"));
        final var februaryEleventh =
                new ImmutablePair<>(parse("2022-02-11T12:00:00"), parse("2022-02-11T12:30:00"));

        initiateRequest(hearingSessionsJsonNode);

        assertHearingDayDetails(List.of(februaryTenth, februaryEleventh));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldInitiateRequest_shouldStoreSingleHearingSessionForSameDateAndDifferentDates() throws Exception {

        JsonNode hearingSessionsJsonNode = OBJECT_MAPPER.readTree(String.format(HEARING, 1,
                createHearingSessions(
                        List.of("2022-02-10T10:30:00", "2022-02-10T12:00:00", "2022-02-11T14:30:00"),
                        List.of("2022-02-10T11:30:00", "2022-02-10T12:30:00", "2022-02-11T16:30:00"))
        ));

        final var februaryTenth =
                new ImmutablePair<>(parse("2022-02-10T10:30:00"), parse("2022-02-10T12:30:00"));
        final var februaryEleventh =
                new ImmutablePair<>(parse("2022-02-11T14:30:00"), parse("2022-02-11T16:30:00"));

        initiateRequest(hearingSessionsJsonNode);

        assertHearingDayDetails(List.of(februaryTenth, februaryEleventh));
    }

    private void assertHearingDayDetails(List<ImmutablePair<LocalDateTime, LocalDateTime>> expectedPairs) {
        final Spliterator<HearingDayDetailsEntity> hearingDayDetailsEntities =
                hearingDayDetailsRepository.findAll().spliterator();

        assertEquals(expectedPairs.size(), hearingDayDetailsEntities.estimateSize());

        final List<ImmutablePair<LocalDateTime, LocalDateTime>> hearingSessionStartAndEndTimes =
                stream(hearingDayDetailsEntities, false)
                        .map(hearingDayDetailsEntity ->
                                ImmutablePair.of(hearingDayDetailsEntity.getStartDateTime(),
                                        hearingDayDetailsEntity.getEndDateTime()))
                        .collect(Collectors.toUnmodifiableList());

        assertTrue(hearingSessionStartAndEndTimes.containsAll(expectedPairs));
    }
}
