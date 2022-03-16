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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    @InjectMocks
    private InboundQueueServiceImpl inboundQueueService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HmiHearingResponseMapper hmiHearingResponseMapper;

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
        inboundQueueService = new InboundQueueServiceImpl(OBJECT_MAPPER, hearingRepository, hmiHearingResponseMapper);
    }

    @Nested
    @DisplayName("ProcessInboundMessage")
    class ProcessInboundMessage {

        @Test
        void shouldThrowHearingNotFoundException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");

            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties));
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());

        }

        @Test
        void shouldThrowMalformedIdException() {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "1000000000");

            Exception exception = assertThrows(BadRequestException.class, () ->
                inboundQueueService.processMessage(jsonNode, applicationProperties));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());

        }

        @Test
        void shouldProcessMessage() throws JsonProcessingException {
            Map<String, Object> applicationProperties = new HashMap<>();
            applicationProperties.put(HEARING_ID, "2000000000");
            HearingEntity hearingEntity = generateHearingEntity(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L))
                .thenReturn(java.util.Optional.of(hearingEntity));

            when(hmiHearingResponseMapper.mapHmiHearingToEntity(any(), any())).thenReturn(hearingEntity);
            inboundQueueService.processMessage(jsonNode, applicationProperties);
            verify(hearingRepository).save(hearingEntity);
        }

        @Test
        void shouldProcessMessageWithErrors() throws JsonProcessingException {
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

            inboundQueueService.processMessage(jsonNode);
        }
    }

    private HearingEntity generateHearingEntity(Long hearingId) {
        HearingEntity entity = new HearingEntity();
        entity.setId(hearingId);
        return entity;
    }
}
