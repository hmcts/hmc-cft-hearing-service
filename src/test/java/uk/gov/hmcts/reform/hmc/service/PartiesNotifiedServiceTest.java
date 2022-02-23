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
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedServiceTest {

    @InjectMocks
    private PartiesNotifiedServiceImpl partiesNotifiedService;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    HearingResponseRepository hearingResponseRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        partiesNotifiedService =
            new PartiesNotifiedServiceImpl(hearingRepository, hearingResponseRepository);
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
                                                          "1", null
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
                                                          "14", null
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

            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
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
                                                          "1", LocalDateTime.now()
                ));

            Exception exception = assertThrows(PartiesNotifiedBadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("003 Already set", exception.getMessage());
        }

    }

    private HearingResponseEntity generateHearingResponseEntity(Long hearingId,
                                                                String responseVersion,
                                                                LocalDateTime dateTime) {
        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setResponseVersion(Integer.valueOf(responseVersion));
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
