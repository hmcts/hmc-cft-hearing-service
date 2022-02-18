package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        partiesNotifiedService =
            new PartiesNotifiedServiceImpl(
                hearingRepository);
    }

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {
        @Test
        void shouldVerifySubsequentCalls() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(1, jsonNode);
            Optional<HearingEntity> hearingEntity =
                Optional.of(generateHearingEntity(1L, "HearingRequested",
                                                  2, "1", null
                ));
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(hearingEntity);

            partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified);
            verify(hearingRepository, times(1)).findById(2000000000L);

        }


        @Test
        @Disabled
        void shouldFailWithInvalidHearingId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(1, jsonNode);

            Exception exception = assertThrows(BadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(1000000000L, 1, partiesNotified));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailWithNoResponseVersion() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(1, jsonNode);
            Optional<HearingEntity> hearingEntity =
                Optional.of(generateHearingEntity(1L, "HearingRequested",
                                                  2, "12", LocalDateTime.now()
                ));
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(hearingEntity);

            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("002 No such response version", exception.getMessage());
        }

        @Test
        @Disabled
        void shouldFailWithNoSuchId() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(1, jsonNode);
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("001 No such id: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithAlreadySet() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = generatePartiesNotified(1, jsonNode);
            Optional<HearingEntity> hearingEntity =
                Optional.of(generateHearingEntity(1L, "HearingRequested",
                                                  2, "1", LocalDateTime.now()
                ));
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(hearingEntity);

            Exception exception = assertThrows(PartiesNotifiedBadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("003 Already set", exception.getMessage());
        }

    }

    private HearingEntity generateHearingEntity(Long hearingId,
                                                String status,
                                                Integer versionNumber,
                                                String responseVersion,
                                                LocalDateTime dateTime) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setVersionNumber(versionNumber);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setResponseVersion(responseVersion);
        hearingResponseEntity.setPartiesNotifiedDateTime(dateTime);
        hearingEntity.setHearingResponses(Arrays.asList(hearingResponseEntity));
        return hearingEntity;
    }

    private PartiesNotified generatePartiesNotified(int requestVersion, JsonNode serviceData) {
        PartiesNotified partiesNotified = new PartiesNotified();
        partiesNotified.setServiceData(serviceData);
        partiesNotified.setRequestVersion(requestVersion);
        return partiesNotified;
    }
}
