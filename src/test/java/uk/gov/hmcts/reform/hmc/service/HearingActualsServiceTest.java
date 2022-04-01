package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HearingActualsServiceTest {

    @InjectMocks
    private HearingActualsServiceImpl hearingActualsService;

    @Mock
    HearingRepository hearingRepository;

    HearingIdValidator hearingIdValidator;

    @Mock
    GetHearingActualsResponseMapper getHearingActualsResponseMapper;

    @Mock
    ApplicationParams applicationParams;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator = new HearingIdValidator(hearingRepository);
        hearingActualsService =
            new HearingActualsServiceImpl(
                hearingRepository,
                getHearingActualsResponseMapper,
                hearingIdValidator
            );
    }

    @Nested
    @DisplayName("getHearing")
    class GetHearing {
        @Test
        void shouldFailWithInvalidHearingId() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> {
                hearingActualsService.getHearingActuals(2000000000L);
            });
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidHearingIdFormat() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(1L);

            Exception exception = assertThrows(BadRequestException.class, () ->
                hearingActualsService.getHearingActuals(1000000000L));
            assertEquals("Invalid hearing Id", exception.getMessage());
        }


        @Test
        void shouldPassWithValidHearingIdInDb() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearing));
            hearingActualsService.getHearingActuals(2000000000L);
            verify(hearingRepository).existsById(2000000000L);
            verify(getHearingActualsResponseMapper).toHearingActualResponse(hearing);
        }

        @Test
        void shouldFailWithInvalidHearingIdForGetHearing() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000010L);

            Exception exception = assertThrows(HearingNotFoundException.class, () ->
                hearingActualsService.getHearingActuals(2000000010L));
            assertEquals("No hearing found for reference: 2000000010", exception.getMessage());
        }
    }


}
