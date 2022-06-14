package uk.gov.hmcts.reform.hmc.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ListAssistResponseException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    @InjectMocks
    private InboundQueueServiceImpl inboundQueueService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ActualHearingRepository actualHearingRepository;

    @Mock
    private ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    private HmiHearingResponseMapper hmiHearingResponseMapper;
    @Mock
    ServiceBusReceiverClient client;

    @Mock
    ServiceBusReceivedMessage serviceBusReceivedMessage;

    @Mock
    ObjectMapperService objectMapperService;

    HearingIdValidator hearingIdValidator;

    @Mock
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

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
                                                   + "        \"key\": \"<key>\",\n"
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
                                                   + "    \"hearingSessions\": [{\n"
                                                   + "    }]\n"
                                                   + "  }\n"
                                                   + "}");

    InboundQueueServiceTest() throws JsonProcessingException {
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository, actualHearingRepository,
                actualHearingDayRepository);
        inboundQueueService = new InboundQueueServiceImpl(
            OBJECT_MAPPER,
            hearingRepository,
            hmiHearingResponseMapper,
            messageSenderToTopicConfiguration,
            objectMapperService,
            hearingIdValidator
        );
    }

    @Nested
    @DisplayName("ProcessInboundMessage")
    class ProcessInboundMessage {

        @Test
        void shouldUpdateHearingStatusWhenCatchingException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            ResourceNotFoundException exception =
                new ResourceNotFoundException("Cannot find latest case hearing request for hearing 2000000000");

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));

            ListAppender<ILoggingEvent> listAppender = setupLogger();

            inboundQueueService.catchExceptionAndUpdateHearing(applicationProperties, exception);

            assertDynatraceLogMessage(listAppender, "2000000000");

            verify(hearingRepository, times(1)).findById(2000000000L);
            verify(hearingRepository, times(1)).save(any());
        }

        @Test
        void shouldNotUpdateHearingStatusWhenCatchingException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            ResourceNotFoundException exception =
                new ResourceNotFoundException("Cannot find latest case hearing request for hearing 2000000000");

            inboundQueueService.catchExceptionAndUpdateHearing(applicationProperties, exception);
            verify(hearingRepository, times(0)).findById(2000000000L);
            verify(hearingRepository, times(0)).save(any());
        }

        @Test
        void shouldThrowHearingNotFoundException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());

        }

        @Test
        void shouldProcessErrorAndUpdateToException() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);

            JsonNode data = OBJECT_MAPPER.convertValue(
                generateErrorDetails("Unable to create case", 2000),
                JsonNode.class);

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingErrorToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(data);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any());

            ListAppender<ILoggingEvent> listAppender = setupLogger();

            inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage);

            assertDynatraceLogMessage(listAppender, "2000000000");
        }

        @Test
        void shouldProcessHearingResponseFromListAssistAndUpdateToException() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.LA_SYNC_HEARING_RESPONSE);

            JsonNode syncJsonNode = OBJECT_MAPPER.readTree("{\n"
                                                                + " \"listAssistHttpStatus\": 200,\n"
                                                                + " \"listAssistErrorCode\": 2000,\n"
                                                                + " \"listAssistErrorDescription\": "
                                                                + "      \"unable to create case\"\n"
                                                                + "}");

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            hearingEntity.setStatus(HearingStatus.EXCEPTION.name());
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(syncJsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiSyncResponseToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(hearingRepository.save(any()))
                .thenReturn(hearingEntity);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any());

            ListAppender<ILoggingEvent> listAppender = setupLogger();

            inboundQueueService.processMessage(syncJsonNode, applicationProperties, client, serviceBusReceivedMessage);

            assertDynatraceLogMessage(listAppender, "2000000000");
        }

        @Test
        void shouldThrowMalformedIdException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());

        }

        @Test
        void shouldThrowMissingHearingIdForHearingResponse() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hearingRepository, times(0)).save(any());
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(0)).existsById(any());
            verify(hearingRepository, times(0)).findById(any());
        }

        @Test
        void shouldProcessHearingResponseMessage() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.AWAITING_LISTING));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any());

            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hearingRepository).save(hearingEntity);
            verify(hmiHearingResponseMapper, times(1)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
        }

        @Test
        void shouldProcessMultiDayHearingResponseMessage() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

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
                                                           + "      \"code\": 100,\n"
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
                                                           + "        \"key\": \"<key>\",\n"
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
                                                           + "    \"hearingSessions\": [{\n"
                                                           + "      \"hearingStartTime\": \"2021-08-10T12:20:00\",\n"
                                                           + "      \"hearingEndTime\": \"2021-08-10T12:20:00\""
                                                           + "    }]\n"
                                                           + "  }\n"
                                                           + "}");

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.AWAITING_LISTING));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any());

            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hearingRepository).save(hearingEntity);
            verify(hmiHearingResponseMapper, times(1)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
        }

        @Test
        void shouldProcessHearingResponseMessageWithErrors() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
            JsonNode jsonNode = OBJECT_MAPPER.readTree("{\n"
                                                           + "  \"meta\": {\n"
                                                           + "    \"transactionIdCaseHQ\": \"<transactionIdCaseHQ>\"\n"
                                                           + "  },\n"
                                                           + "  \"hearing\": {\n"
                                                           + "    \"listingRequestId\": \"<listingRequestId>\",\n"
                                                           + "    \"hearingCaseVersionId\": 10,\n"
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
                                                           + "        \"key\": \"<key>\",\n"
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
                                                           + "    \"hearingSessions\": [{\n"
                                                           + "    }]\n"
                                                           + "  }\n"
                                                           + "}");
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(0)).findById(2000000000L);
        }


        @Test
        void shouldProcessHearingResponseMessageWithHearingCodeErrors() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
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
                                                           + "      \"code\": \"200\",\n"
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
                                                           + "        \"key\": \"<key>\",\n"
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
                                                           + "    \"hearingSessions\": [{\n"
                                                           + "    }]\n"
                                                           + "  }\n"
                                                           + "}");
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(0)).findById(2000000000L);
        }

        @Test
        @Disabled
        void shouldProcessErrorResponseWithNoIssues() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            applicationProperties.put(HEARING_ID, "2000000000");
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");

            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingErrorToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any());

            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(ListAssistResponseException.class, () ->
                inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals("Error received for hearing Id: 2000000000 with an "
                             + "error message of 2000 Unable to create case", exception.getMessage());
        }

        @Test
        void shouldThrowHearingNotFoundExceptionForErrorPayload() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());

        }

        @Test
        void shouldThrowMalformedIdExceptionForErrorPayload() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());

        }

        @Test
        void shouldThrowMissingHearingIdForErrorPayload() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);

            inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage);
            verify(hearingRepository, times(0)).save(any());
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingErrorToEntity(any(), any());
            verify(hearingRepository, times(0)).existsById(any());
            verify(hearingRepository, times(0)).findById(any());
        }
    }

    private HearingEntity generateHearingEntity(Long hearingId) {
        HearingEntity entity = new HearingEntity();
        entity.setId(hearingId);

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setRequestVersion(1);
        entity.setHearingResponses(List.of(hearingResponseEntity));
        return entity;
    }

    private HmcHearingResponse generateHmcResponse(uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus status) {
        HmcHearingResponse hmcHearingResponse = new HmcHearingResponse();
        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHmcStatus(status.name());
        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        return hmcHearingResponse;
    }

    private ErrorDetails generateErrorDetails(String description, int code) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorDescription(description);
        errorDetails.setErrorCode(code);
        return errorDetails;
    }

    private ListAppender<ILoggingEvent> setupLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger(InboundQueueServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }

    private void assertDynatraceLogMessage(ListAppender<ILoggingEvent> listAppender, String hearingID) {
        List<ILoggingEvent> logsList = listAppender.list;
        int finalErrorIndex = logsList.size() - 1;
        assertEquals(Level.ERROR, logsList.get(finalErrorIndex).getLevel());
        assertEquals("Hearing id: " + hearingID + " updated to status Exception",
                     logsList.get(finalErrorIndex).getMessage());
    }
}
