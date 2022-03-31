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
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedServiceTest extends PartiesNotifiedCommonGeneration {

    @InjectMocks
    private PartiesNotifiedServiceImpl partiesNotifiedService;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    HearingResponseRepository hearingResponseRepository;

    private HearingIdValidator hearingIdValidator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository);
        partiesNotifiedService =
            new PartiesNotifiedServiceImpl(hearingResponseRepository,
                                           hearingIdValidator);
    }

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {
        @Test
        void shouldVerifySubsequentCalls() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(2000000000L))
                .thenReturn(generateHearingResponseEntity(2000000000L,
                                                          1, null
                ));

            partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified);
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingResponseRepository, times(1))
                .getHearingResponse(2000000000L);

        }


        @Test
        void shouldFailWithInvalidHearingId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);

            Exception exception = assertThrows(BadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(1000000000L, 1, partiesNotified));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailWithNoResponseVersion() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(2000000000L))
                .thenReturn(generateHearingResponseEntity(2000000000L,
                                                          14, null
                ));

            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("002 No such response version", exception.getMessage());
        }

        @Test
        void shouldFailWithNoSuchId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("001 No such id: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithAlreadySet() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(jsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingResponseRepository.getHearingResponse(2000000000L))
                .thenReturn(generateHearingResponseEntity(2000000000L,
                                                          1, LocalDateTime.now()
                ));

            Exception exception = assertThrows(PartiesNotifiedBadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("003 Already set", exception.getMessage());
        }

    }

    @Nested
    @DisplayName("GetPartiesNotified")
    class GetPartiesNotified {
        @Test
        void shouldFindPartiesNotifiedForValidHearingId() {
            final Long hearingId = 2000000001L;
            List<HearingResponseEntity> partiesNotifiedAnswer = generateEntities(hearingId);

            when(hearingRepository.existsById(hearingId)).thenReturn(true);
            when(hearingResponseRepository.getPartiesNotified(any())).thenReturn(partiesNotifiedAnswer);

            PartiesNotifiedResponses partiesNotifiedResponses =
                partiesNotifiedService.getPartiesNotified(hearingId);
            assertFalse(partiesNotifiedResponses.getResponses().isEmpty());
            assertEquals(3, partiesNotifiedResponses.getResponses().size());
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
                                                                Integer responseVersion,
                                                                LocalDateTime dateTime) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setResponseVersion(responseVersion);
        hearingResponseEntity.setPartiesNotifiedDateTime(dateTime);
        hearingResponseEntity.setHearingResponseId(hearingId);
        return hearingResponseEntity;
    }

    private PartiesNotified generatePartiesNotified(JsonNode serviceData) {
        PartiesNotified partiesNotified = new PartiesNotified();
        partiesNotified.setServiceData(serviceData);
        return partiesNotified;
    }
}
