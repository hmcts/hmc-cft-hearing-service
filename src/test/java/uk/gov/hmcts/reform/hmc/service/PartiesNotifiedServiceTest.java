package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
class PartiesNotifiedServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(PartiesNotifiedServiceTest.class);

    @InjectMocks
    private PartiesNotifiedServiceImpl partiesNotifiedService;

    @Mock
    HearingRepository hearingRepository;

    JsonNode jsonNode = mock(JsonNode.class);

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
        void shouldVerifySubsequentCalls() {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            PartiesNotified partiesNotified = generatePartiesNotified(1, json);
            Optional<HearingEntity> hearingEntity =
                Optional.of(generateHearingEntity(1L, "HearingRequested",
                                                  2, "1", null
                ));
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(hearingEntity);

            partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified);
            verify(hearingRepository, times(1)).existsById(2000000000L);
            verify(hearingRepository, times(1)).findById(2000000000L);

        }

        @Test
        void shouldFailWithInvalidHearingId() {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            PartiesNotified partiesNotified = generatePartiesNotified(1, json);

            Exception exception = assertThrows(BadRequestException.class, () ->
                partiesNotifiedService.getPartiesNotified(1000000000L, 1, partiesNotified));
            assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
        }

        @Test
        void shouldFailWithNoResponseVersion() {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            PartiesNotified partiesNotified = generatePartiesNotified(1, json);
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
        void shouldFailWithNoSuchId() {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            PartiesNotified partiesNotified = generatePartiesNotified(1, json);
            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(PartiesNotifiedNotFoundException.class, () ->
                partiesNotifiedService.getPartiesNotified(2000000000L, 1, partiesNotified));
            assertEquals("001 No such id: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithAlreadySet() {
            String json = "{\"query\": {\"match\": \"blah blah\"}}";
            PartiesNotified partiesNotified = generatePartiesNotified(1, json);
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


    /**
     * generate Hearing Entity.
     *
     * @param hearingId Hearing Id
     * @param status    status
     * @return hearingEntity Hearing Entity
     */
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

    private PartiesNotified generatePartiesNotified(int requestVersion, Object serviceData) {
        PartiesNotified partiesNotified = new PartiesNotified();
        partiesNotified.setServiceData(serviceData);
        partiesNotified.setRequestVersion(requestVersion);
        return partiesNotified;
    }
}
