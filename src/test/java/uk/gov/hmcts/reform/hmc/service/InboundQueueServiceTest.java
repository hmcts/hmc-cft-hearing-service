package uk.gov.hmcts.reform.hmc.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.client.hmi.ErrorDetails;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.ListAssistResponseException;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

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
import static uk.gov.hmcts.reform.hmc.service.InboundQueueServiceImpl.MISSING_HEARING_ID;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    @InjectMocks
    private InboundQueueServiceImpl inboundQueueService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HmiHearingResponseMapper hmiHearingResponseMapper;
    @Mock
    ServiceBusReceiverClient client;

    @Mock
    ServiceBusReceivedMessage serviceBusReceivedMessage;

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
                                                   + "      \"code\": \"LISTED\",\n"
                                                   + "      \"description\": \"<description>\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingIdCaseHQ\": \"<hearingIdCaseHQ>\",\n"
                                                   + "    \"hearingType\": {\n"
                                                   + "      \"test\": \"value\"\n"
                                                   + "    },\n"
                                                   + "    \"hearingStatus\": {\n"
                                                   + "      \"code\": \"<code>\",\n"
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

    InboundQueueServiceTest() throws JsonProcessingException {
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        inboundQueueService = new InboundQueueServiceImpl(
            OBJECT_MAPPER,
            hearingRepository,
            hmiHearingResponseMapper,
            messageSenderToTopicConfiguration
        );
    }

    @Nested
    @DisplayName("ProcessInboundMessage")
    class ProcessInboundMessage {

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
        void shouldThrowMalformedIdException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());

        }

        @Test
        void shouldThrowMissingHearingIdForHearingResponse() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.HEARING_RESPONSE);

            Exception exception = assertThrows(MalformedMessageException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals(MISSING_HEARING_ID, exception.getMessage());

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
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any());

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
                                                           + "      \"code\": \"LISTED\",\n"
                                                           + "      \"description\": \"<description>\"\n"
                                                           + "    },\n"
                                                           + "    \"hearingIdCaseHQ\": \"<hearingIdCaseHQ>\",\n"
                                                           + "    \"hearingType\": {\n"
                                                           + "      \"test\": \"value\"\n"
                                                           + "    },\n"
                                                           + "    \"hearingStatus\": {\n"
                                                           + "      \"code\": \"<code>\",\n"
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
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            inboundQueueService.processMessage(jsonNode, applicationProperties, client, serviceBusReceivedMessage);
            verify(hmiHearingResponseMapper, times(0)).mapHmiHearingToEntity(any(), any());
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(0)).findById(2000000000L);
        }

        @Test
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
            doNothing().when(messageSenderToTopicConfiguration).sendMessage(any());

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
        void shouldThrowMissingHearingIdForErrorPayload() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(MESSAGE_TYPE, MessageType.ERROR);
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorCode(2000);
            errorDetails.setErrorDescription("Unable to create case");
            JsonNode data = OBJECT_MAPPER.convertValue(errorDetails, JsonNode.class);
            Exception exception = assertThrows(MalformedMessageException.class, () ->
                inboundQueueService.processMessage(data, applicationProperties, client, serviceBusReceivedMessage));
            assertEquals(MISSING_HEARING_ID, exception.getMessage());

        }
    }

    private HearingEntity generateHearingEntity(Long hearingId) {
        HearingEntity entity = new HearingEntity();
        entity.setId(hearingId);

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
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
}
