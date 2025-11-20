package uk.gov.hmcts.reform.hmc.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.client.hmi.SyncResponse;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_ACK;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_RESPONSE;

@Slf4j
class InboundQueueServiceIT extends BaseTest {

    private static final String APP_PROPERTY_HEARING_ID = "hearing_id";
    private static final String APP_PROPERTY_MESSAGE_TYPE = "message_type";

    private static final long HEARING_ID = 2000000000L;
    private static final long NON_EXISTING_HEARING_ID = 2500000000L;

    private static final String HMCTS_SERVICE_ID = "ABA1";

    private static final String SCRIPT_DELETE_HEARING_TABLES = "classpath:sql/delete-hearing-tables.sql";
    private static final String SCRIPT_INSERT_HEARING_INBOUND_QUEUE = "classpath:sql/insert-hearing-inbound-queue.sql";

    @Mock
    private ServiceBusReceivedMessageContext serviceBusReceivedMessageContext;

    @Mock
    private ServiceBusReceivedMessage serviceBusReceivedMessage;

    private final ObjectMapper objectMapper;

    private final HearingRepository hearingRepository;

    private final HearingStatusAuditRepository hearingStatusAuditRepository;

    private final InboundQueueService inboundQueueService;

    @Autowired
    public InboundQueueServiceIT(InboundQueueService inboundQueueService,
                                 HearingRepository hearingRepository,
                                 HearingStatusAuditRepository hearingStatusAuditRepository) {
        this.inboundQueueService = inboundQueueService;
        this.hearingRepository = hearingRepository;
        this.hearingStatusAuditRepository = hearingStatusAuditRepository;

        objectMapper = new Jackson2ObjectMapperBuilder().modules(new Jdk8Module()).build();
    }

    @Test
    void processMessage_HearingIdInvalidFormat() throws JsonProcessingException {
        Map<String, Object> appProperties = createApplicationProperties(1L, MessageType.LA_SYNC_HEARING_RESPONSE);

        String syncResponseSuccess = """
            {
                "listAssistHttpStatus": 202
            }""";
        JsonNode message = objectMapper.readTree(syncResponseSuccess);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> inboundQueueService.processMessage(message, serviceBusReceivedMessageContext));

        assertEquals("Invalid hearing Id", exception.getMessage(), "Bad request exception has unexpected message");

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES})
    void processMessage_HearingIdDoesNotExist() throws JsonProcessingException {
        Map<String, Object> appProperties =
            createApplicationProperties(NON_EXISTING_HEARING_ID, MessageType.LA_SYNC_HEARING_RESPONSE);

        String syncResponseSuccess = """
            {
                "listAssistHttpStatus": 202
            }""";
        JsonNode message = objectMapper.readTree(syncResponseSuccess);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        HearingNotFoundException exception =
            assertThrows(HearingNotFoundException.class,
                         () -> inboundQueueService.processMessage(message, serviceBusReceivedMessageContext));

        assertEquals("No hearing found for reference: 2500000000",
                     exception.getMessage(),
                     "Hearing not found exception has unexpected message");

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void processMessage_MessageTypeHearingResponse() throws JsonProcessingException {
        Map<String, Object> appProperties = createApplicationProperties(MessageType.HEARING_RESPONSE);

        String hearingResponse = """
            {
                "meta": {
                    "timestamp": "2025-11-01T12:00:00",
                    "transactionIdCaseHQ": "<transactionIdCaseHQ>"
                },
                "hearing": {
                    "listingRequestId": "<listingRequestId>",
                    "hearingCaseVersionId": 1,
                    "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                    "hearingCaseJurisdiction": {"test": "value"},
                    "hearingCaseStatus": {"code": "6", "description": "<description>"},
                    "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                    "hearingType": {"test": "value"},
                    "hearingStatus": {"code": "DRAFT", "description": "<description>"},
                    "hearingCancellationReason": "<hearingCancellationReason>",
                    "hearingStartTime": "2025-12-01T12:00:00",
                    "hearingEndTime": "2025-12-01T13:00:00",
                    "hearingPrivate": true,
                    "hearingRisk": true,
                    "hearingTranslatorRequired": false,
                    "hearingCreatedDate": "2025-10-01T12:00:00",
                    "hearingCreatedBy": "testuser",
                    "hearingVenue": {
                        "locationIdCaseHQ": "<locationIdCaseHQ>",
                        "locationName": "<locationName>",
                        "locationRegion": "<locationRegion>",
                        "locationCluster": "<locationCluster>",
                        "locationReferences": [{"key": "EPIMS", "value": "<valueEPIMS>"}]
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
                        "entityId": "<entityId>",
                        "entityType": "<type>",
                        "entityClass": "<class>",
                        "entityRole": {"key": "<key>", "value": "<value>"},
                        "hearingChannel": {"code": "<keyHearingChannel>", "description": "<value>"}
                    }],
                    "hearingJohs": [{
                        "johId": "<johId>",
                        "johCode": "<johCode>",
                        "johName": "<johName>",
                        "johPosition": {"key": "<key>", "value": "<value>"},
                        "isPresiding": false
                    }],
                    "hearingSessions": []
                }
            }""";
        JsonNode message = objectMapper.readTree(hearingResponse);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        inboundQueueService.processMessage(message, serviceBusReceivedMessageContext);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, null, "HEARING_REQUESTED", null);

        List<HearingResponseEntity> hearingResponses = hearing.getHearingResponses();
        assertEquals(1, hearingResponses.size(), "Hearing has unexpected number of hearing responses");

        HearingResponseEntity hearingResponseEntity = hearingResponses.getFirst();
        assertHearingResponse(hearingResponseEntity);

        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertEquals(1,
                     hearingStatusAuditEntities.size(),
                     "Unexpected number of hearing status audit entities");
        assertHearingStatusAuditHearingResponse(hearingStatusAuditEntities.getFirst(), hearing);

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void processMessage_MessageTypeHearingResponse_ValidationError() throws JsonProcessingException {
        Map<String, Object> appProperties = createApplicationProperties(MessageType.HEARING_RESPONSE);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        String incompleteHearingResponse = """
            {
                "meta": {
                    "timestamp": "2025-11-01T12:00:00",
                    "transactionIdCaseHQ": "<transactionIdCaseHQ>"
                },
                "hearing": {
                }
            }""";
        JsonNode message = objectMapper.readTree(incompleteHearingResponse);

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        inboundQueueService.processMessage(message, serviceBusReceivedMessageContext);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, null, "HEARING_REQUESTED", null);

        assertTrue(hearing.getHearingResponses().isEmpty(), "No hearing response should be created for hearing");

        assertNoHearingStatusAuditForHearing();

        List<ILoggingEvent> logList = listAppender.list;
        assertNotNull(logList, "Log should not be null");
        assertEquals(4, logList.size(), "Unexpected number of entries in log");

        List<LogEntry> expectedLogEntries = new ArrayList<>();
        expectedLogEntries.add(new LogEntry(Level.INFO, "Message of type HEARING_RESPONSE received"));
        expectedLogEntries.add(new LogEntry(Level.INFO, "Total violations found: 2"));
        expectedLogEntries.add(new LogEntry(Level.ERROR,
                                            "Violations are Hearing case version id can not be null or empty"));
        expectedLogEntries.add(new LogEntry(Level.ERROR, "Violations are Hearing case status not be null or empty"));

        assertLogEntries(logList, expectedLogEntries);

        logger.detachAndStopAllAppenders();

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void processMessage_MessageTypeSyncResponse_Success() throws JsonProcessingException {
        Map<String, Object> appProperties = createApplicationProperties(MessageType.LA_SYNC_HEARING_RESPONSE);

        String syncResponseSuccess = """
            {
                "listAssistHttpStatus": 202
            }""";
        JsonNode message = objectMapper.readTree(syncResponseSuccess);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        inboundQueueService.processMessage(message, serviceBusReceivedMessageContext);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, null, "AWAITING_LISTING", "AWAITING_LISTING");

        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertEquals(1,
                     hearingStatusAuditEntities.size(),
                     "Unexpected number of hearing status audit entities");
        assertHearingStatusAuditSyncResponse(hearingStatusAuditEntities.getFirst(), hearing, 202, null);

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void processMessage_MessageTypeSyncResponse_Error() throws JsonProcessingException {
        // Note: Scenario will no longer occur as outbound adapter does not write HMI error responses to inbound queue
        Map<String, Object> appProperties = createApplicationProperties(MessageType.LA_SYNC_HEARING_RESPONSE);

        SyncResponse syncResponseError = new SyncResponse(400, 9999, "Some error");
        JsonNode message = objectMapper.valueToTree(syncResponseError);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        inboundQueueService.processMessage(message, serviceBusReceivedMessageContext);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, 9999, "Some error", "EXCEPTION", null);

        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertEquals(1,
                     hearingStatusAuditEntities.size(),
                     "Unexpected number of hearing status audit entities");
        assertHearingStatusAuditSyncResponse(hearingStatusAuditEntities.getFirst(), hearing, 400, syncResponseError);

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void processMessage_MessageTypeError() throws JsonProcessingException {
        Map<String, Object> appProperties = createApplicationProperties(MessageType.ERROR);

        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(9999);
        errorDetails.setErrorDescription("Some error");
        JsonNode message = objectMapper.valueToTree(errorDetails);

        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);

        inboundQueueService.processMessage(message, serviceBusReceivedMessageContext);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, 9999, "Some error", "EXCEPTION", null);

        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertEquals(1,
                     hearingStatusAuditEntities.size(),
                     "Unexpected number of hearing status audit entities");
        assertHearingStatusAuditErrorDetails(hearingStatusAuditEntities.getFirst(), hearing, errorDetails);

        verify(serviceBusReceivedMessageContext).getMessage();
        verify(serviceBusReceivedMessage).getApplicationProperties();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void catchExceptionAndUpdateHearing_HearingUpdated() {
        Map<String, Object> appProperties = createApplicationProperties(MessageType.LA_SYNC_HEARING_RESPONSE);

        IllegalArgumentException exception = new IllegalArgumentException("An exception message");
        final JsonNode expectedJsonNode = objectMapper.convertValue(exception.getMessage(), JsonNode.class);

        inboundQueueService.catchExceptionAndUpdateHearing(appProperties, exception);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, "An exception message", "EXCEPTION", null);

        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertEquals(1,
                     hearingStatusAuditEntities.size(),
                     "Unexpected number of hearing status audit entities");
        assertHearingStatusAuditJsonNode(hearingStatusAuditEntities.getFirst(), hearing, expectedJsonNode);
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void catchExceptionAndUpdateHearing_NoHearingId() {
        Map<String, Object> appPropertiesEmpty = new HashMap<>();
        IllegalArgumentException exception = new IllegalArgumentException("An exception message");

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        inboundQueueService.catchExceptionAndUpdateHearing(appPropertiesEmpty, exception);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, null, "HEARING_REQUESTED", null);

        assertNoHearingStatusAuditForHearing();

        List<ILoggingEvent> logList = listAppender.list;
        assertNotNull(logList, "Log should not be null");
        assertEquals(1, logList.size(), "Unexpected number of entries in log");

        List<LogEntry> expectedLogEntries = new ArrayList<>();
        expectedLogEntries.add(
            new LogEntry(Level.ERROR, "Error processing message Message is missing custom header hearing_id")
        );

        assertLogEntries(logList, expectedLogEntries);

        logger.detachAndStopAllAppenders();
    }

    @Test
    @Sql(scripts = {SCRIPT_DELETE_HEARING_TABLES, SCRIPT_INSERT_HEARING_INBOUND_QUEUE})
    void catchExceptionAndUpdateHearing_HearingIdDoesNotExist() {
        Map<String, Object> appProperties =
            createApplicationProperties(NON_EXISTING_HEARING_ID, MessageType.LA_SYNC_HEARING_RESPONSE);

        IllegalArgumentException exception = new IllegalArgumentException("An exception message");

        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        inboundQueueService.catchExceptionAndUpdateHearing(appProperties, exception);

        HearingEntity hearing = getHearing();
        assertHearingErrorAndStatus(hearing, null, null, "HEARING_REQUESTED", null);

        assertNoHearingStatusAuditForHearing();

        List<ILoggingEvent> logList = listAppender.list;
        assertNotNull(logList, "Log should not be null");
        assertEquals(2, logList.size(), "Unexpected number of entries in log");

        List<LogEntry> expectedLogEntries = new ArrayList<>();
        expectedLogEntries.add(
            new LogEntry(Level.ERROR,
                         "Error processing message with Hearing id 2500000000 exception was An exception message")
        );
        expectedLogEntries.add(new LogEntry(Level.ERROR, "Hearing id 2500000000 not found"));

        assertLogEntries(logList, expectedLogEntries);

        logger.detachAndStopAllAppenders();
    }

    private Map<String, Object> createApplicationProperties(MessageType messageType) {
        return createApplicationProperties(HEARING_ID, messageType);
    }

    private Map<String, Object> createApplicationProperties(long hearingId, MessageType messageType) {
        Map<String, Object> appProperties = new HashMap<>();

        appProperties.put(APP_PROPERTY_HEARING_ID, String.valueOf(hearingId));
        appProperties.put(APP_PROPERTY_MESSAGE_TYPE, messageType);

        return appProperties;
    }

    private HearingEntity getHearing() {
        Optional<HearingEntity> hearing = hearingRepository.findById(HEARING_ID);
        assertTrue(hearing.isPresent(), "Hearing 2000000000 should be present");
        return hearing.get();
    }

    private void assertHearingErrorAndStatus(HearingEntity hearing,
                                             Integer expectedErrorCode,
                                             String expectedErrorDescription,
                                             String expectedStatus,
                                             String expectedLastGoodStatus) {
        if (expectedErrorCode == null) {
            assertNull(hearing.getErrorCode(), "Hearing error code should be null");
        } else {
            assertEquals(expectedErrorCode, hearing.getErrorCode(), "Hearing has unexpected error code");
        }

        if (expectedErrorDescription == null) {
            assertNull(hearing.getErrorDescription(), "Hearing error description should be null");
        } else {
            assertEquals(expectedErrorDescription,
                         hearing.getErrorDescription(),
                         "Hearing has unexpected error description");
        }

        assertEquals(expectedStatus, hearing.getStatus(), "Hearing has unexpected status");

        if (expectedLastGoodStatus == null) {
            assertNull(hearing.getLastGoodStatus(), "Hearing last good status should be null");
        } else {
            assertEquals(expectedLastGoodStatus,
                         hearing.getLastGoodStatus(),
                         "Hearing has unexpected last good status");
        }
    }

    private void assertHearingResponse(HearingResponseEntity hearingResponse) {
        assertEquals("<transactionIdCaseHQ>",
                     hearingResponse.getListingTransactionId(),
                     "Hearing response listing transaction id has unexpected value");
        assertEquals(LocalDateTime.of(2025, Month.NOVEMBER, 1, 12, 0, 0),
                     hearingResponse.getRequestTimeStamp(),
                     "Hearing response request timestamp has unexpected value");
        assertEquals(1, hearingResponse.getRequestVersion(), "Hearing response request version has unexpected value");
        assertEquals("DRAFT",
                     hearingResponse.getListingStatus(),
                     "Hearing response listing status has unexpected value");
        assertEquals("<hearingCancellationReason>",
                     hearingResponse.getCancellationReasonType(),
                     "Hearing response cancellation reason type has unexpected value");
        assertFalse(hearingResponse.getTranslatorRequired(), "Hearing response translator required should be false");
        assertEquals("PENDING_RELISTING",
                     hearingResponse.getListingCaseStatus(),
                     "Hearing response listing case status has unexpected value");

        List<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getHearingDayDetails();
        assertEquals(1, hearingDayDetails.size(), "Hearing response has unexpected number of hearing day details");

        HearingDayDetailsEntity hearingDayDetailsEntity = hearingDayDetails.getFirst();
        assertEquals(LocalDateTime.of(2025, Month.DECEMBER, 1, 12, 0, 0),
                     hearingDayDetailsEntity.getStartDateTime(),
                     "Hearing day details has unexpected start date time");
        assertEquals(LocalDateTime.of(2025, Month.DECEMBER, 1, 13, 0, 0),
                     hearingDayDetailsEntity.getEndDateTime(),
                     "Hearing day details has unexpected end date time");
        assertEquals("<valueEPIMS>",
                     hearingDayDetailsEntity.getVenueId(),
                     "Hearing day details has unexpected venue id");
        assertEquals("<roomName>", hearingDayDetailsEntity.getRoomId(), "Hearing day details has unexpected room id");

        List<HearingAttendeeDetailsEntity> hearingAttendeeDetails = hearingDayDetailsEntity.getHearingAttendeeDetails();
        assertEquals(1, hearingAttendeeDetails.size(), "Hearing day details has unexpected number of attendee details");

        HearingAttendeeDetailsEntity hearingAttendeeDetailsEntity = hearingAttendeeDetails.getFirst();
        assertEquals("<entityId>",
                     hearingAttendeeDetailsEntity.getPartyId(),
                     "Hearing attendee details has unexpected party id");
        assertEquals("<keyHearingChannel>",
                     hearingAttendeeDetailsEntity.getPartySubChannelType(),
                     "Hearing attendee details has unexpected party sub channel type");

        List<HearingDayPanelEntity> hearingDayPanels = hearingDayDetailsEntity.getHearingDayPanel();
        assertEquals(1, hearingDayPanels.size(), "Hearing day details has unexpected number of hearing day panels");

        HearingDayPanelEntity hearingDayPanelEntity = hearingDayPanels.getFirst();
        assertEquals("<johCode>",
                     hearingDayPanelEntity.getPanelUserId(),
                     "Hearing day panel has unexpected panel user id");
        assertFalse(hearingDayPanelEntity.getIsPresiding(),
                    "Hearing day panel is presiding value should be false");
    }

    private void assertHearingStatusAuditHearingResponse(HearingStatusAuditEntity hearingStatusAudit,
                                                         HearingEntity hearing) {
        assertNull(hearingStatusAudit.getErrorDescription());
        assertHearingStatusAudit(hearingStatusAudit, hearing, LA_RESPONSE, 202, FH, HMC);
    }

    private void assertHearingStatusAuditSyncResponse(HearingStatusAuditEntity hearingStatusAudit,
                                                      HearingEntity hearing,
                                                      Integer httpStatus,
                                                      SyncResponse expectedErrorSyncResponse)
        throws JsonProcessingException {
        JsonNode actualErrorDescription = hearingStatusAudit.getErrorDescription();
        if (expectedErrorSyncResponse == null) {
            assertNull(actualErrorDescription, "Hearing status audit error description should be null");
        } else {
            SyncResponse actualErrorSyncResponse =
                objectMapper.treeToValue(actualErrorDescription, SyncResponse.class);

            assertEquals(expectedErrorSyncResponse.getListAssistHttpStatus(),
                         actualErrorSyncResponse.getListAssistHttpStatus(),
                         "Hearing status audit error description has unexpected list assist HTTP status");

            assertEquals(expectedErrorSyncResponse.getListAssistErrorCode(),
                         actualErrorSyncResponse.getListAssistErrorCode(),
                         "Hearing status audit error description has unexpected list assist error code");

            assertEquals(expectedErrorSyncResponse.getListAssistErrorDescription(),
                         actualErrorSyncResponse.getListAssistErrorDescription(),
                         "Hearing status audit error description has unexpected list assist error description");
        }

        assertHearingStatusAudit(hearingStatusAudit, hearing, LA_ACK, httpStatus, HMC, FH);
    }

    private void assertHearingStatusAuditErrorDetails(HearingStatusAuditEntity hearingStatusAudit,
                                                      HearingEntity hearing,
                                                      ErrorDetails expectedErrorDetails)
        throws JsonProcessingException {
        JsonNode actualErrorDescription = hearingStatusAudit.getErrorDescription();
        if (expectedErrorDetails == null) {
            assertNull(actualErrorDescription, "Hearing status audit error description should be null");
        } else {
            ErrorDetails actualErrorDetails =
                objectMapper.treeToValue(actualErrorDescription, ErrorDetails.class);

            assertEquals(expectedErrorDetails.getErrorCode(),
                         actualErrorDetails.getErrorCode(),
                         "Hearing status audit error description has unexpected error code");
            assertEquals(expectedErrorDetails.getErrorDescription(),
                         actualErrorDetails.getErrorDescription(),
                         "Hearing status audit error description has unexpected description");
        }

        assertHearingStatusAudit(hearingStatusAudit, hearing, LA_RESPONSE, 400, FH, HMC);
    }

    private void assertHearingStatusAuditJsonNode(HearingStatusAuditEntity hearingStatusAudit,
                                                  HearingEntity hearing,
                                                  JsonNode expectedJsonNode) {
        JsonNode actualJsonNode = hearingStatusAudit.getErrorDescription();
        assertEquals(expectedJsonNode.toString(), actualJsonNode.toString());

        assertHearingStatusAudit(hearingStatusAudit, hearing, LA_RESPONSE, 400, FH, HMC);
    }

    private void assertHearingStatusAudit(HearingStatusAuditEntity hearingStatusAudit,
                                          HearingEntity hearing,
                                          String event,
                                          Integer httpStatus,
                                          String source,
                                          String target) {
        assertEquals(
            HMCTS_SERVICE_ID,
            hearingStatusAudit.getHmctsServiceId(),
            "Hearing status audit has unexpected HMCTS service id");
        assertEquals(String.valueOf(hearing.getId()),
                     hearingStatusAudit.getHearingId(),
                     "Hearing status audit has unexpected hearing id");
        assertEquals(hearing.getStatus(), hearingStatusAudit.getStatus(), "Hearing status audit has unexpected status");
        assertEquals(event, hearingStatusAudit.getHearingEvent(), "Hearing status audit has unexpected hearing event");
        assertEquals(String.valueOf(httpStatus),
                     hearingStatusAudit.getHttpStatus(),
                     "Hearing status audit has unexpected HTTP status");
        assertEquals(source, hearingStatusAudit.getSource(), "Hearing status audit has unexpected source");
        assertEquals(target, hearingStatusAudit.getTarget(), "Hearing status audit has unexpected target");
        assertEquals("1",
                     hearingStatusAudit.getRequestVersion(),
                     "Hearing status audit request version has unexpected value");
        assertNotNull(hearingStatusAudit.getResponseDateTime(),
                      "Hearing status audit response date time should not be null");
        assertNull(hearingStatusAudit.getOtherInfo(), "Hearing status audit other info should be null");
    }

    private void assertNoHearingStatusAuditForHearing() {
        List<HearingStatusAuditEntity> hearingStatusAuditEntities =
            hearingStatusAuditRepository.findByHearingId(String.valueOf(HEARING_ID));
        assertTrue(hearingStatusAuditEntities.isEmpty(),
                   "No hearing status audit entities should be created for hearing 2000000000");
    }

    private void assertLogEntries(List<ILoggingEvent> logList, List<LogEntry> expectedLogEntries) {
        for (LogEntry logEntry : expectedLogEntries) {
            assertTrue(logList.stream()
                           .anyMatch(loggingEvent -> loggingEvent.getLevel() == logEntry.level()
                               && loggingEvent.getFormattedMessage().equals(logEntry.message())),
                       "Log should contain entry with level '" + logEntry.level()
                           + "' and message '" + logEntry.message() + "'");
        }
    }

    private record LogEntry(Level level, String message) {}
}
