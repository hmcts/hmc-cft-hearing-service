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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
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
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FINAL_STATE_MESSAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.MESSAGE_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    @InjectMocks
    private InboundQueueServiceImpl inboundQueueService;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ActualHearingRepository actualHearingRepository;

    @Mock
    private ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    private HmiHearingResponseMapper hmiHearingResponseMapper;

    @Mock
    private ServiceBusReceivedMessageContext messageContext = mock(ServiceBusReceivedMessageContext.class);

    @Mock
    ObjectMapperService objectMapperService;

    HearingIdValidator hearingIdValidator;

    @Mock
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private HearingStatusAuditService hearingStatusAuditService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    JsonNode jsonNode = OBJECT_MAPPER.readTree("""
               {
                 "meta": {
                   "transactionIdCaseHQ": "<transactionIdCaseHQ>",
                   "timestamp": "2021-08-10T12:20:00"
                 },
                 "hearing": {
                   "listingRequestId": "<listingRequestId>",
                   "hearingCaseVersionId": 10,
                   "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                   "hearingCaseJurisdiction": {
                     "test": "value"
                   },
                   "hearingCaseStatus": {
                     "code": "100",
                     "description": "<description>"
                   },
                   "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                   "hearingType": {
                     "test": "value"
                   },
                   "hearingStatus": {
                     "code": "DRAFT",
                     "description": "<descrixption>"
                   },
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
                     "locationReferences": [{
                       "key": "<key>",
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
                   },
                   "hearingAttendees": [{
                     "entityIdCaseHQ": "<id>",
                     "entityId": "<id>",
                     "entityType": "<type>",
                     "entityClass": "<class>",
                     "entityRole": {
                       "key": "<key>",
                       "value": "<value>"
                     },
                     "hearingChannel": {
                       "code": "<key>",
                       "description": "<value>"
                     }
                   }],
                   "hearingJohs": [{
                     "johId": "<johId>",
                     "johCode": "<johCode>",
                     "johName": "<johName>",
                     "johPosition": {
                       "key": "<key>",
                       "value": "<value>"
                     },
                     "isPresiding": false
                   }],
                   "hearingSessions": [{}]
                 }
               }
               """);

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
            hearingIdValidator,
            applicationParams,
            hearingStatusAuditService
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

            HearingEntity hearingEntity = generateHearingEntity(2000000000L, null, "");
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));

            ListAppender<ILoggingEvent> listAppender = setupLogger();
            inboundQueueService.catchExceptionAndUpdateHearing(applicationProperties, exception);
            assertDynatraceLogMessage(listAppender, "2000000000", "1111222233334444",
                                      "TEST", "Cannot find latest case hearing request for hearing 2000000000");

            verify(hearingRepository, times(1)).findById(2000000000L);
            verify(hearingRepository, times(1)).save(any());
            verify(hearingStatusAuditService, times(1))
                .saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(any());
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
            verify(hearingStatusAuditService, times(0))
                .saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(any());
        }

        @Test
        void shouldThrowHearingNotFoundException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);

            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                inboundQueueService.processMessage(jsonNode, messageContext));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());

        }

        @Test
        void shouldProcessErrorAndUpdateToException() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            HearingEntity hearingEntity = generateHearingEntity(2000000000L,500,
                                                                "Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(
                generateErrorDetails("Unable to create case", 2000),
                JsonNode.class);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingErrorToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(data);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any(), any(), any());
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            ListAppender<ILoggingEvent> listAppender = setupLogger();
            inboundQueueService.processMessage(data, messageContext);
            assertDynatraceLogMessage(listAppender, "2000000000", "1111222233334444",
                                      "TEST", "Unable to create case");
        }

        @Test
        void shouldProcessHearingResponseFromListAssistAndUpdateToException() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.LA_SYNC_HEARING_RESPONSE);
            HearingEntity hearingEntity = generateHearingEntity(2000000000L,400,
                                                                "Unable to create case");
            hearingEntity.setStatus(HearingStatus.EXCEPTION.name());
            JsonNode syncJsonNode = OBJECT_MAPPER.readTree("""
                                   {
                                     "listAssistHttpStatus": 200,
                                     "listAssistErrorCode": 2000,
                                     "listAssistErrorDescription": "unable to create case"
                                   }
                                   """);
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(syncJsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiSyncResponseToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(hearingRepository.save(any()))
                .thenReturn(hearingEntity);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any(), any(), any());
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            ListAppender<ILoggingEvent> listAppender = setupLogger();
            inboundQueueService.processMessage(syncJsonNode, messageContext);
            assertDynatraceLogMessage(listAppender, "2000000000", "1111222233334444",
                                      "TEST", "Unable to create case");
        }

        @Test
        void shouldThrowMalformedIdException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);

            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(jsonNode, messageContext));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());

        }

        @Test
        void shouldThrowMissingHearingIdForHearingResponse() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);

            inboundQueueService.processMessage(jsonNode, messageContext);
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

            HearingEntity hearingEntity = generateHearingEntity(2000000000L,null, "");
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.AWAITING_LISTING));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any(),any(), any());

            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);

            inboundQueueService.processMessage(jsonNode, messageContext);
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

            JsonNode jsonNodeLOcal = OBJECT_MAPPER.readTree("""
                           {
                             "meta": {
                               "transactionIdCaseHQ": "<transactionIdCaseHQ>",
                               "timestamp": "2021-08-10T12:20:00"
                             },
                             "hearing": {
                               "listingRequestId": "<listingRequestId>",
                               "hearingCaseVersionId": 10,
                               "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                               "hearingCaseJurisdiction": {
                                 "test": "value"
                               },
                               "hearingCaseStatus": {
                                 "code": 100,
                                 "description": "<description>"
                               },
                               "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                               "hearingType": {
                                 "test": "value"
                               },
                               "hearingStatus": {
                                 "code": "DRAFT",
                                 "description": "<description>"
                               },
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
                                 "locationReferences": [{
                                   "key": "<key>",
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
                               },
                               "hearingAttendees": [{
                                 "entityIdCaseHQ": "<id>",
                                 "entityId": "<id>",
                                 "entityType": "<type>",
                                 "entityClass": "<class>",
                                 "entityRole": {
                                   "key": "<key>",
                                   "value": "<value>"
                                 },
                                 "hearingChannel": {
                                   "code": "<key>",
                                   "description": "<value>"
                                 }
                               }],
                               "hearingJohs": [{
                                 "johId": "<johId>",
                                 "johCode": "<johCode>",
                                 "johName": "<johName>",
                                 "johPosition": {
                                   "key": "<key>",
                                   "value": "<value>"
                                 },
                                 "isPresiding": false
                               }],
                               "hearingSessions": [{
                                 "hearingStartTime": "2021-08-10T12:20:00",
                                 "hearingEndTime": "2021-08-10T12:20:00"
                               }]
                             }
                           }
                           """);
            HearingEntity hearingEntity = generateHearingEntity(2000000000L,null, "");
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.AWAITING_LISTING));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNodeLOcal);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any(), any(), any());
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            inboundQueueService.processMessage(jsonNodeLOcal, messageContext);
            verify(hearingRepository).save(hearingEntity);
            verify(hmiHearingResponseMapper, times(1)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(2)).findById(2000000000L);
        }

        @ParameterizedTest
        @EnumSource(value = HearingStatus.class, names = {"CANCELLED", "ADJOURNED", "COMPLETED"})
        void shouldProcessHearingResponseMessageWhenHearingIsInTerminalState(HearingStatus terminalStatus)
            throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            final JsonNode jsonNodeLocal = OBJECT_MAPPER.readTree("""
                                                                {
                                                                  "meta": {
                                                                    "transactionIdCaseHQ": "<transactionIdCaseHQ>",
                                                                    "timestamp": "2021-08-10T12:20:00"
                                                                  },
                                                                  "hearing": {
                                                                    "listingRequestId": "<listingRequestId>",
                                                                    "hearingCaseVersionId": 10,
                                                                    "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                                                                    "hearingCaseJurisdiction": {
                                                                      "test": "value"
                                                                    },
                                                                    "hearingCaseStatus": {
                                                                      "code": 100,
                                                                      "description": "Closed"
                                                                    },
                                                                    "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                                                                    "hearingType": {
                                                                      "test": "value"
                                                                    },
                                                                    "hearingStatus": {
                                                                      "code": "DRAFT",
                                                                      "description": "<description>"
                                                                    },
                                                                    "hearingCancellationReason": "<reason>",
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
                                                                      "locationReferences": [{
                                                                        "key": "<key>",
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
                                                                    },
                                                                    "hearingAttendees": [{
                                                                      "entityIdCaseHQ": "<id>",
                                                                      "entityId": "<id>",
                                                                      "entityType": "<type>",
                                                                      "entityClass": "<class>",
                                                                      "entityRole": {
                                                                        "key": "<key>",
                                                                        "value": "<value>"
                                                                      },
                                                                      "hearingChannel": {
                                                                        "code": "<key>",
                                                                        "description": "<value>"
                                                                      }
                                                                    }],
                                                                    "hearingJohs": [{
                                                                      "johId": "<johId>",
                                                                      "johCode": "<johCode>",
                                                                      "johName": "<johName>",
                                                                      "johPosition": {
                                                                        "key": "<key>",
                                                                        "value": "<value>"
                                                                      },
                                                                      "isPresiding": false
                                                                    }],
                                                                    "hearingSessions": [{
                                                                      "hearingStartTime": "2021-08-10T12:20:00",
                                                                      "hearingEndTime": "2021-08-10T12:20:00"
                                                                    }]
                                                                  }
                                                                }
                                                                """);
            HearingEntity hearingEntity = generateHearingEntity(2000000000L, null, "");
            hearingEntity.setStatus(terminalStatus.name());
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            ListAppender<ILoggingEvent> listAppender = setupLogger();
            inboundQueueService.processMessage(jsonNodeLocal, messageContext);
            assertDynatraceLogMessageForTerminalState(listAppender, "2000000000", "1111222233334444",
                                      "TEST", terminalStatus.name(),"Closed");
            verify(hearingRepository, never()).save(any());
            verify(hmiHearingResponseMapper, never()).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).findById(2000000000L);
            verify(hearingStatusAuditService, times(1))
                .saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(any());
            verify(messageSenderToTopicConfiguration, times(0))
                .sendMessage(any(), any(), any(), any());
        }

        @Test
        void shouldProcessHearingResponseMessageWithErrors() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            JsonNode jsonNodeLocal = OBJECT_MAPPER.readTree("""
                       {
                         "meta": {
                           "transactionIdCaseHQ": "<transactionIdCaseHQ>"
                         },
                         "hearing": {
                           "listingRequestId": "<listingRequestId>",
                           "hearingCaseVersionId": 10,
                           "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                           "hearingCaseJurisdiction": {
                             "test": "value"
                           },
                           "hearingCaseStatus": {
                             "code": "100",
                             "description": "<description>"
                           },
                           "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                           "hearingType": {
                             "test": "value"
                           },
                           "hearingStatus": {
                             "code": "DRAFT",
                             "description": "<descrixption>"
                           },
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
                             "locationReferences": [{
                               "key": "<key>",
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
                           },
                           "hearingAttendees": [{
                             "entityIdCaseHQ": "<id>",
                             "entityId": "<id>",
                             "entityType": "<type>",
                             "entityClass": "<class>",
                             "entityRole": {
                               "key": "<key>",
                               "value": "<value>"
                             },
                             "hearingChannel": {
                               "code": "<key>",
                               "description": "<value>"
                             }
                           }],
                           "hearingJohs": [{
                             "johId": "<johId>",
                             "johCode": "<johCode>",
                             "johName": "<johName>",
                             "johPosition": {
                               "key": "<key>",
                               "value": "<value>"
                             },
                             "isPresiding": false
                           }],
                           "hearingSessions": [{}]
                         }
                       }
                       """);
            inboundQueueService.processMessage(jsonNodeLocal, messageContext);
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(0)).findById(2000000000L);
        }


        @Test
        void shouldProcessHearingResponseMessageWithHearingCodeErrors() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            JsonNode jsonNodeLocal = OBJECT_MAPPER.readTree("""
                                   {
                                     "meta": {
                                       "transactionIdCaseHQ": "<transactionIdCaseHQ>",
                                       "timestamp": "2021-08-10T12:20:00"
                                     },
                                     "hearing": {
                                       "listingRequestId": "<listingRequestId>",
                                       "hearingCaseVersionId": 10,
                                       "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                                       "hearingCaseJurisdiction": {
                                         "test": "value"
                                       },
                                       "hearingCaseStatus": {
                                         "code": "200",
                                         "description": "<description>"
                                       },
                                       "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                                       "hearingType": {
                                         "test": "value"
                                       },
                                       "hearingStatus": {
                                         "code": "DRAFT",
                                         "description": "<descrixption>"
                                       },
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
                                         "locationReferences": [{
                                           "key": "<key>",
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
                                       },
                                       "hearingAttendees": [{
                                         "entityIdCaseHQ": "<id>",
                                         "entityId": "<id>",
                                         "entityType": "<type>",
                                         "entityClass": "<class>",
                                         "entityRole": {
                                           "key": "<key>",
                                           "value": "<value>"
                                         },
                                         "hearingChannel": {
                                           "code": "<key>",
                                           "description": "<value>"
                                         }
                                       }],
                                       "hearingJohs": [{
                                         "johId": "<johId>",
                                         "johCode": "<johCode>",
                                         "johName": "<johName>",
                                         "johPosition": {
                                           "key": "<key>",
                                           "value": "<value>"
                                         },
                                         "isPresiding": false
                                       }],
                                       "hearingSessions": [{}]
                                     }
                                   }
                                   """);
            inboundQueueService.processMessage(jsonNodeLocal, messageContext);
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(0)).findById(2000000000L);
        }

        @Test
        @Disabled
        void shouldProcessErrorResponseWithNoIssues() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            applicationProperties.put(HEARING_ID, "2000000000");
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            HearingEntity hearingEntity = generateHearingEntity(2000000000L,2000,
                                                                "Unable to create case");
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));
            when(hmiHearingResponseMapper.mapHmiHearingErrorToEntity(any(), any())).thenReturn(hearingEntity);
            when(hmiHearingResponseMapper.mapEntityToHmcModel(any(), any()))
                .thenReturn(generateHmcResponse(HearingStatus.EXCEPTION));
            when(objectMapperService.convertObjectToJsonNode(any())).thenReturn(jsonNode);
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any(), any(),any(), any());

            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(ListAssistResponseException.class, () ->
                inboundQueueService.processMessage(data, messageContext));
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
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                inboundQueueService.processMessage(data, messageContext));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void shouldThrowMalformedIdExceptionForErrorPayload() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(data, messageContext));
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
            given(messageContext.getMessage()).willReturn(message);
            given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
            inboundQueueService.processMessage(data, messageContext);
            verify(hearingRepository, times(0)).save(any());
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingErrorToEntity(any(), any());
            verify(hearingRepository, times(0)).existsById(any());
            verify(hearingRepository, times(0)).findById(any());
        }
    }

    @Test
    void shouldProcessHearingResponseForHearingStatusMaxLength() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        HearingEntity hearingEntity = generateHearingEntity(2000000000L,1000,
                                                            "status code error");
        hearingEntity.setStatus(HearingStatus.EXCEPTION.name());
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        JsonNode jsonNodeLocal = OBJECT_MAPPER.readTree("""
{
"meta": {
"transactionIdCaseHQ": "<transactionIdCaseHQ>",
"timestamp": "2021-08-10T12:20:00"
},
"hearing": {
"listingRequestId": "<listingRequestId>",
"hearingCaseVersionId": 10,
"hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
"hearingCaseJurisdiction": {
  "test": "value"
},
"hearingCaseStatus": {
  "code": "100",
  "description": "<description>"
},
"hearingIdCaseHQ": "<hearingIdCaseHQ>",
"hearingType": {
  "test": "value"
},
"hearingStatus": {
  "code": "Unlikely to be movedFixed - Unlikely to be movedFixed - Unlikely to be movedFixed - Unlikely to be moved",
  "description": "<description>"
},
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
  "locationReferences": [{
    "key": "<key>",
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
},
"hearingAttendees": [{
  "entityIdCaseHQ": "<id>",
  "entityId": "<id>",
  "entityType": "<type>",
  "entityClass": "<class>",
  "entityRole": {
    "key": "<key>",
    "value": "<value>"
  },
  "hearingChannel": {
    "code": "<key>",
    "description": "<value>"
  }
}],
"hearingJohs": [{
  "johId": "<johId>",
  "johCode": "<johCode>",
  "johName": "<johName>",
  "johPosition": {
    "key": "<key>",
    "value": "<value>"
  },
  "isPresiding": false
}],
"hearingSessions": [{}]
}
}
                                                            """);
        ListAppender<ILoggingEvent> listAppender = setupLogger();
        inboundQueueService.processMessage(jsonNodeLocal, messageContext);
        assertLogMessageForHearingStatusMaxLength(listAppender,
            "Violations are Hearing status code must not be more than 30 characters long");
    }

    @Test
    void shouldProcessHearingResponseForHearingStatusNull() throws JsonProcessingException {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(HEARING_ID, "2000000000");
        applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);
        HearingEntity hearingEntity = generateHearingEntity(2000000000L,400,
                                                            "Violations are Hearing status");
        hearingEntity.setStatus(null);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        hearingEntity.setStatus(HearingStatus.EXCEPTION.name());
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        given(messageContext.getMessage()).willReturn(message);
        given(messageContext.getMessage().getApplicationProperties()).willReturn(applicationProperties);
        JsonNode jsonNodeLocal = OBJECT_MAPPER.readTree("""
            {
              "meta": {
                "transactionIdCaseHQ": "<transactionIdCaseHQ>",
                "timestamp": "2021-08-10T12:20:00"
              },
              "hearing": {
                "listingRequestId": "<listingRequestId>",
                "hearingCaseVersionId": 10,
                "hearingCaseIdHMCTS": "<hearingCaseIdHMCTS>",
                "hearingCaseJurisdiction": {
                  "test": "value"
                },
                "hearingCaseStatus": {
                  "code": "100",
                  "description": "<description>"
                },
                "hearingIdCaseHQ": "<hearingIdCaseHQ>",
                "hearingType": {
                  "test": "value"
                },
                "hearingStatus": {
                  "code": "",
                  "description": "<descrixption>"
                },
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
                  "locationReferences": [{
                    "key": "<key>",
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
                },
                "hearingAttendees": [{
                  "entityIdCaseHQ": "<id>",
                  "entityId": "<id>",
                  "entityType": "<type>",
                  "entityClass": "<class>",
                  "entityRole": {
                    "key": "<key>",
                    "value": "<value>"
                  },
                  "hearingChannel": {
                    "code": "<key>",
                    "description": "<value>"
                  }
                }],
                "hearingJohs": [{
                  "johId": "<johId>",
                  "johCode": "<johCode>",
                  "johName": "<johName>",
                  "johPosition": {
                    "key": "<key>",
                    "value": "<value>"
                  },
                  "isPresiding": false
                }],
                "hearingSessions": [{}]
              }
            }
            """);
        ListAppender<ILoggingEvent> listAppender = setupLogger();
        inboundQueueService.processMessage(jsonNodeLocal, messageContext);
        assertLogMessageForHearingStatusMaxLength(listAppender,
                          "Violations are Hearing status code can not be null or empty");
    }

    private void assertLogMessageForHearingStatusMaxLength(ListAppender<ILoggingEvent> listAppender,
                                                           String errorMessage) {
        List<ILoggingEvent> logsList = listAppender.list;
        int finalErrorIndex = logsList.size() - 1;
        assertEquals(Level.ERROR, logsList.get(finalErrorIndex).getLevel());
        assertEquals(errorMessage, logsList.get(finalErrorIndex).getFormattedMessage());
    }

    private HearingEntity generateHearingEntity(Long hearingId, Integer errorCode, String errorDescription) {
        HearingEntity entity = new HearingEntity();
        entity.setId(hearingId);
        entity.setErrorCode(errorCode);
        entity.setErrorDescription(errorDescription);
        entity.setStatus(HearingStatus.AWAITING_LISTING.name());
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setRequestVersion(1);
        entity.setHearingResponses(List.of(hearingResponseEntity));
        entity.setCaseHearingRequests(List.of(TestingUtil.caseHearingRequestEntity()));
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

    private void assertDynatraceLogMessage(ListAppender<ILoggingEvent> listAppender, String hearingID,  String caseRef,
                                           String serviceCode, String errorDescription) {
        List<ILoggingEvent> logsList = listAppender.list;
        int finalErrorIndex = logsList.size() - 1;
        assertEquals(Level.ERROR, logsList.get(finalErrorIndex).getLevel());
        assertEquals("Hearing id: " + hearingID + " with Case reference: "
                         + caseRef + " , Service Code: " + serviceCode + " and Error Description: "
                         + errorDescription + " updated to status "
                         + HearingStatus.EXCEPTION.name(), logsList.get(finalErrorIndex).getFormattedMessage());
    }

    private void assertDynatraceLogMessageForTerminalState(ListAppender<ILoggingEvent> listAppender, String hearingID,
                                                           String caseRef, String serviceCode, String currentStatus,
                                                           String terminalStatus) {
        List<ILoggingEvent> logsList = listAppender.list;
        int finalErrorIndex = logsList.size() - 1;
        assertEquals(Level.INFO, logsList.get(finalErrorIndex).getLevel());
        assertEquals(FINAL_STATE_MESSAGE, logsList.get(finalErrorIndex).getMessage());
        assertEquals("Hearing id: " + hearingID + " with Case reference: "
                         + caseRef + " , Service Code: " + serviceCode + " and Response received but "
                         + "current hearing status: "
                         + currentStatus + "; LA status: " + terminalStatus + " no further action taken ",
                     logsList.get(finalErrorIndex).getFormattedMessage());
    }
}
