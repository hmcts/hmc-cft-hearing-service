package uk.gov.hmcts.reform.hmc.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.repository.HearingDayDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.InboundQueueService;
import uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.LocalDateTime.parse;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CFT_HEARING_SERVICE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR_PROCESSING_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC_FROM_HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.READ;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.formatLogMessage;

class MessageProcessorIT extends BaseTest {

    @Inject
    private HearingDayDetailsRepository hearingDayDetailsRepository;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private ServiceBusReceivedMessageContext messageContext = mock(ServiceBusReceivedMessageContext.class);

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .findModulesViaServiceLoader(true)
        .build();
    private static final String MESSAGE_TYPE = "message_type";
    private static final String HEARING_ID = "hearing_id";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request_hmi.sql";
    private static final String HEARING = """
        {
          "meta": {
            "transactionIdCaseHQ": "<transactionIdCaseHQ>",
            "timestamp": "2021-08-10T12:20:00"
          },
          "hearing": {
            "listingRequestId": "<listingRequestId>",
            "hearingCaseVersionId": %s,
            "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
            "hearingCaseJurisdiction": {"test": "value"},
            "hearingCaseStatus": {"code": "100", "description": "<description>"},
            "hearingIdCaseHQ": "<hearingIdCaseHQ>",
            "hearingType": {"test": "value"},
            "hearingStatus": {"code": "DRAFT", "description": "<description>"},
            "hearingCancellationReason": "<hearingCancellationReason>",
            "hearingStartTime": "2021-08-10T12:20:00",
            "hearingEndTime": "2021-08-10T12:20:00",
            "hearingPrivate": true,
            "hearingRisk": true,
            "hearingTranslatorRequired": false,
            "hearingCreatedDate": "2021-08-10T12:20:00",
            "hearingCreatedBy": "testuser",
            "hearingVenue": {
              "locationIdCaseHQ": "<locationIdCaseHQ>",
              "locationName": "<locationName>",
              "locationRegion": "<locationRegion>",
              "locationCluster": "<locationCluster>",
              "locationReferences": [{"key": "EPIMS", "value": "<value>"}]
            },
            "hearingRoom": {
              "locationIdCaseHQ": "<locationIdCaseHQ>",
              "locationName": "<roomName>",
              "locationRegion": {"key": "<key>", "value": "<value>"},
              "locationCluster": {"key": "<key>", "value": "<value>"},
              "locationReferences": {"key": "<key>", "value": "<value>"}
            },
            "hearingAttendees": [{
              "entityIdCaseHQ": "<id>",
              "entityId": "<id>",
              "entityType": "<type>",
              "entityClass": "<class>",
              "entityRole": {"key": "<key>", "value": "<value>"},
              "hearingChannel": {"code": "<key>", "description": "<value>"}
            }],
            "hearingJohs": [{
              "johId": "<johId>",
              "johCode": "<johCode>",
              "johName": "<johName>",
              "johPosition": {"key": "<key>", "value": "<value>"},
              "isPresiding": false
            }],
            "hearingSessions": %s
          }
        }
        """;

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
        return String.format("""
                        {
                            "hearingStartTime": "%s",
                            "hearingEndTime": "%s",
                            "hearingVenue": {
                                "locationIdCaseHQ": "<locationIdCaseHQ>",
                                "locationName": "<locationName>",
                                "locationRegion": "<locationRegion>",
                                "locationCluster": "<locationCluster>",
                                "locationReferences": [{
                                    "key": "EPIMS",
                                    "value": "<value>"
                                }]
                            },
                            "hearingRoom": {
                                "locationIdCaseHQ": "<locationIdCaseHQ>",
                                "locationName": "<roomName>",
                                "locationRegion": {
                                    "key": "<key>",
                                    "value": "<value>"
                                },
                                "locationCluster": {
                                    "key": "<key>",
                                    "value": "<value>"
                                },
                                "locationReferences": {
                                    "key": "<key>",
                                    "value": "<value>"
                                }
                            }
                        }
                        """,
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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonNode, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Message of type " + MessageType.HEARING_RESPONSE.name() + " received",
                     logsList.get(0).getFormattedMessage());
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

        JsonNode errorJsonNode = OBJECT_MAPPER.readTree("""
                 {
                    "errCode": 2000,
                    "errDesc": "unable to create case"
                 }
                """);
        MessageProcessor messageProcessor = new MessageProcessor(OBJECT_MAPPER, inboundQueueService);
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        messageProcessor.processMessage(errorJsonNode, messageContext);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(3, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Message of type " + MessageType.ERROR.name() + " received",
                     logsList.get(0).getFormattedMessage());
        assertEquals(Level.INFO, logsList.get(1).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals(
            "Hearing id: 2000000000 with Case reference: 9372710950276233 , Service Code: "
                + "TEST and Error Description: unable to create case updated to status "
                + HearingStatus.EXCEPTION.name(),
            logsList.get(2).getFormattedMessage()
        );
        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        logsListMessageProcessor.forEach(System.out::print);
        // There could be message entity not found error due to the way the pipeline structure works with our variables
        // so count the errors against the message entity not found error.
        // NOTE this should not account for any other exceptions.
        assertEquals(logsListMessageProcessor.stream().filter(log -> log.getLevel().equals(Level.ERROR)).count(),
                logsListMessageProcessor.stream()
                    .filter(log -> log.getThrowableProxy().getMessage().contains("The messaging entity")).count());
        assertFalse(logsListMessageProcessor.stream().anyMatch(log -> log.getLevel().equals(Level.INFO)));
        assertFalse(logsListMessageProcessor.stream().anyMatch(log -> log.getLevel().equals(Level.WARN)));
    }

    @Test
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void shouldThrowMismatchOnRequestVersion() {
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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonMisMatchOnRequestVersion, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(3, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.ERROR, logsList.get(1).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals("Message of type " + MessageType.HEARING_RESPONSE.name() + " received",
                     logsList.get(0).getFormattedMessage());
        assertEquals("Error processing message with Hearing id 2000000000 exception was "
                         + "Cannot find request version 10 for hearing 2000000000",
                     logsList.get(1).getFormattedMessage());
        assertEquals(
            "Hearing id: 2000000000 with Case reference: 9372710950276233 , Service Code: TEST "
                + "and Error Description: Cannot find request version 10 for hearing 2000000000"
                + " updated to status " + HearingStatus.EXCEPTION.name(), logsList.get(2).getFormattedMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertEquals(2, logsListMessageProcessor.size());
        assertTrue(logsListMessageProcessor.stream().allMatch(log -> log.getLevel().equals(Level.ERROR)));
        assertEquals("Error for message with id null with error Cannot find request version 10 for hearing 2000000000",
                logsListMessageProcessor.get(0).getFormattedMessage());
        assertEquals(formatLogMessage(ERROR_PROCESSING_MESSAGE,CFT_HEARING_SERVICE, HMC_FROM_HMI, READ,
                                      2000000000), logsListMessageProcessor.get(1).getFormattedMessage());
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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonNode, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(0, logsList.size());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        logger.info("{} : {}", logsListMessageProcessor.size(), logsListMessageProcessor.toArray());
        assertFalse(logsListMessageProcessor.isEmpty());
        List<Level> levels = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        logsListMessageProcessor.forEach(e ->  {
            levels.add(e.getLevel());
            messages.add(e.getMessage());
        });
        logsListMessageProcessor.forEach(e ->  levels.add(e.getLevel()));
        assertTrue(levels.contains(Level.ERROR));
        assertTrue(messages.contains(
                "Message is missing custom header message_type for message with message with id null"));
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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonNode, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.ERROR, logsList.get(1).getLevel());
        assertEquals("Message of type " + MessageType.HEARING_RESPONSE.name() + " received",
                     logsList.get(0).getFormattedMessage());
        assertEquals("Error processing message, exception was Message is missing custom header hearing_id",
                     logsList.get(1).getFormattedMessage());

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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonNode, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Message of type " + MessageType.HEARING_RESPONSE.name() + " received",
                     logsList.get(0).getFormattedMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertTrue(logsListMessageProcessor.stream().anyMatch(log -> log.getLevel().equals(Level.ERROR)));
        assertTrue(logsListMessageProcessor.stream().anyMatch(log -> log.getFormattedMessage()
                .matches(
                        "Error for message with id null with error No hearing found for reference: 2000000001")));
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

        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        try {
            messageProcessor.processMessage(jsonNode, messageContext);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(3, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());
        assertEquals("Message of type " + MessageType.HEARING_RESPONSE.name() + " received",
                     logsList.get(0).getFormattedMessage());
        assertEquals("Error processing message with Hearing id 1000000000 exception was "
                         + "Invalid hearing Id", logsList.get(1).getFormattedMessage());
        assertEquals("Hearing id 1000000000 not found", logsList.get(2).getFormattedMessage());

        List<ILoggingEvent> logsListMessageProcessor = listAppenderMessageProcessor.list;
        assertTrue(logsListMessageProcessor.stream().anyMatch(log -> log.getLevel().equals(Level.ERROR)));
        assertTrue(logsListMessageProcessor.stream().anyMatch(log -> log.getFormattedMessage()
                .matches("Error for message with id null with error Invalid hearing Id")));
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

        assertEquals(2, hearingDayDetailsEntities.spliterator().estimateSize());
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
        final var hearingDetails =
            new ImmutablePair<>(parse("2021-08-10T12:20:00"), parse("2021-08-10T12:20:00"));

        initiateRequest(hearingSessionsJsonNode);

        assertHearingDayDetails(List.of(februaryTenth, februaryEleventh, hearingDetails));
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
        final var hearingDetails =
            new ImmutablePair<>(parse("2021-08-10T12:20:00"), parse("2021-08-10T12:20:00"));


        initiateRequest(hearingSessionsJsonNode);

        assertHearingDayDetails(List.of(februaryTenth, februaryEleventh, hearingDetails));
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
                        .toList();

        assertTrue(hearingSessionStartAndEndTimes.containsAll(expectedPairs));
    }
}
