package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.PartiesNotifiedCommonGeneration;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_NO_SUCH_RESPONSE;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedServiceTest extends PartiesNotifiedCommonGeneration {

    @InjectMocks
    private PartiesNotifiedServiceImpl partiesNotifiedService;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    ActualHearingRepository actualHearingRepository;

    @Mock
    ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    HearingResponseRepository hearingResponseRepository;

    private HearingIdValidator hearingIdValidator;

    @Mock
    private HearingStatusAuditService hearingStatusAuditService;

    private static final String CLIENT_S2S_TOKEN = "s2s_token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository,
                actualHearingRepository, actualHearingDayRepository);
        partiesNotifiedService =
            new PartiesNotifiedServiceImpl(hearingResponseRepository,
                                           hearingIdValidator,
                                           hearingStatusAuditService);
    }

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {
        @Test
        void shouldVerifySubsequentCalls() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);

            final Long hearingId = 2000000000L;
            final Integer requestVersion = 1;
            final LocalDateTime receivedDateTime = LocalDateTime.now();
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(hearingId, requestVersion, receivedDateTime))
                .thenReturn(generateHearingResponseEntity(hearingId,
                                                          requestVersion,
                                                            receivedDateTime,
                                                            null
                ));

            partiesNotifiedService.getPartiesNotified(hearingId, requestVersion,
                    receivedDateTime, partiesNotified, CLIENT_S2S_TOKEN);
            verify(hearingRepository, times(1)).existsById(hearingId);
            verify(hearingResponseRepository, times(1))
                .getHearingResponse(hearingId, requestVersion, receivedDateTime);

        }


        @Test
        void shouldFailWithInvalidHearingId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);

            LocalDateTime dateTime = LocalDateTime.now();
            Exception exception = assertThrows(BadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(1000000000L, 1,
                        dateTime, partiesNotified, CLIENT_S2S_TOKEN));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailWithNoResponseVersion() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(anyLong())).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(anyLong(), anyInt(), any()))
                .thenReturn(null);
            LocalDateTime dateTime = LocalDateTime.now();
            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1,
                        dateTime, partiesNotified, CLIENT_S2S_TOKEN));
            assertEquals(PARTIES_NOTIFIED_NO_SUCH_RESPONSE, exception.getMessage());
        }

        @Test
        void shouldFailWithNoSuchId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            LocalDateTime dateTime = LocalDateTime.now();
            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1,
                        dateTime, partiesNotified, CLIENT_S2S_TOKEN));
            assertEquals("001 No such id: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithAlreadySet() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(anyLong())).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(anyLong(), anyInt(), any()))
                .thenReturn(generateHearingResponseEntity(2000000000L,
                                                          1,
                                                            LocalDateTime.now(),
                                                            LocalDateTime.now()
                ));

            LocalDateTime dateTime = LocalDateTime.now();
            Exception exception = assertThrows(PartiesNotifiedBadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1,
                        dateTime, partiesNotified, CLIENT_S2S_TOKEN));
            assertEquals("003 Already set", exception.getMessage());
        }

    }

    @Nested
    @DisplayName("GetPartiesNotified")
    class GetPartiesNotified {
        @Test
        void shouldFindPartiesNotifiedForValidHearingId() {
            final Long hearingId = 2000000001L;
            List<HearingResponseEntity> partiesNotifiedAnswer = generateEntitiesForPartiesNotified(hearingId);

            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingResponseRepository.getPartiesNotified(any())).thenReturn(partiesNotifiedAnswer);

            PartiesNotifiedResponses partiesNotifiedResponses =
                partiesNotifiedService.getPartiesNotified(hearingId);
            assertFalse(partiesNotifiedResponses.getResponses().isEmpty());
            assertThat(3).isEqualTo(partiesNotifiedResponses.getResponses().size());
            assertThat(2).isEqualTo(partiesNotifiedResponses.getResponses().get(0).getRequestVersion());
            assertThat(1).isEqualTo(partiesNotifiedResponses.getResponses().get(1).getRequestVersion());
            assertThat(1).isEqualTo(partiesNotifiedResponses.getResponses().get(2).getRequestVersion());
            LocalDateTime now = LocalDateTime.now();
            assertThat(now.minusDays(2).toLocalDate()).isEqualTo(partiesNotifiedResponses.getResponses()
                                                  .get(0).getResponseReceivedDateTime().toLocalDate());
            assertThat(now.minusDays(1).toLocalDate()).isEqualTo(partiesNotifiedResponses.getResponses()
                                                  .get(1).getResponseReceivedDateTime().toLocalDate());
            assertThat(now.minusDays(3).toLocalDate()).isEqualTo(partiesNotifiedResponses.getResponses()
                                                    .get(2).getResponseReceivedDateTime().toLocalDate());
        }

        @Test
        void shouldNotFindPartiesNotifiedForValidHearingId() {
            final Long hearingId = 2000000001L;
            List<HearingResponseEntity> partiesNotifiedDateTimesAnswer = new ArrayList<>();
            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingResponseRepository.getPartiesNotified(hearingId)).thenReturn(partiesNotifiedDateTimesAnswer);
            PartiesNotifiedResponses partiesNotifiedDateTimes = partiesNotifiedService.getPartiesNotified(hearingId);
            assertTrue(partiesNotifiedDateTimes.getResponses().isEmpty());
        }

        @Test
        void shouldFindErrorForNullHearingId() {
            final Long hearingId = null;
            shouldFindErrorForInvalidHearingId(hearingId);
        }

        @Test
        void shouldFindErrorForInvalidFormatHearingId() {
            final Long hearingId = 1000000001L;
            shouldFindErrorForInvalidHearingId(hearingId);
        }

        @Test
        void shouldFindErrorForHearingIdNotFound() {
            final Long hearingId = 2000000001L;
            when(hearingRepository.existsById(hearingId)).thenReturn(false);
            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(hearingId)
            );
            assertEquals("001 No such id: " + hearingId, exception.getMessage());
        }

    }

    private void shouldFindErrorForInvalidHearingId(Long hearingId) {
        Exception exception = assertThrows(BadRequestException.class, () ->
            partiesNotifiedService.getPartiesNotified(hearingId)
        );
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    private HearingResponseEntity generateHearingResponseEntity(Long hearingId,
                                                                Integer requestVersion,
                                                                LocalDateTime receivedDateTime,
                                                                LocalDateTime partiesNotifiedDateTime) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setRequestVersion(requestVersion);
        hearingResponseEntity.setRequestTimeStamp(receivedDateTime);
        hearingResponseEntity.setPartiesNotifiedDateTime(partiesNotifiedDateTime);
        hearingResponseEntity.setHearingResponseId(hearingId);
        hearingResponseEntity.setHearing(TestingUtil.hearingEntity());
        return hearingResponseEntity;
    }

    private PartiesNotified generatePartiesNotified(JsonNode serviceData) {
        PartiesNotified partiesNotified = new PartiesNotified();
        partiesNotified.setServiceData(serviceData);
        return partiesNotified;
    }
}
