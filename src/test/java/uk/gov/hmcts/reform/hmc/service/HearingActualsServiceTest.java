package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.actualHearingDay;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingActualsOutcome;

@ExtendWith(MockitoExtension.class)
class HearingActualsServiceTest {
    public static final String VALID_HEARING_STAUS = "UPDATE_REQUESTED";
    public static final long INVALID_HEARING_ID = 1000000000L;
    public static final long HEARING_ID = 2000000000L;

    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private HearingHelper hearingHelper;
    @Mock
    private HearingEntity hearingEntity;

    @InjectMocks
    private HearingActualsServiceImpl hearingActualsService;

    @BeforeEach
    public void setUp() {
        hearingActualsService = new HearingActualsServiceImpl(hearingRepository, hearingHelper);
    }

    @Test
    void shouldUpdateHearingActuals() {
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
        given(hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearingEntity))
            .willReturn(LocalDateTime.of(LocalDate.of(2022, 1, 28), LocalTime.MIN));
        HearingActual hearingActual = TestingUtil.hearingActual();
        hearingActualsService.updateHearingActuals(HEARING_ID, hearingActual);
    }

    @Test
    void shouldThrowExceptionWhenInvalidHearingId() {
        doThrow(new BadRequestException(INVALID_HEARING_ID_DETAILS)).when(hearingHelper).isValidFormat(anyString());
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(INVALID_HEARING_ID, TestingUtil.hearingActual());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoHearingIdFound() {
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual());
        });
        assertEquals("001 No such id: 2000000000", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHearingStatusNotAllowingActuals() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn("HEARING_REQUESTED");
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual());
        });
        assertEquals("002 invalid status HEARING_REQUESTED", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDuplicatesInHearingActualsDays() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID,
                                                       TestingUtil.hearingActualWithDuplicatedHearingDate());
        });
        assertEquals("004 non-unique dates", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOneHearingActualsDayInTheFuture() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActualWithHearingDateInFuture());
        });
        assertEquals("003 invalid date", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultOfAdjournedWithoutHearingResultReasonType() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
        given(hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearingEntity))
            .willReturn(LocalDateTime.of(LocalDate.of(2022, 1, 28), LocalTime.MIN));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual(
                hearingActualsOutcome("ADJOURNED", null),
                List.of(actualHearingDay(LocalDate.of(2022, 1, 28)))
            ));
        });
        assertEquals("ADJOURNED result requires a hearingResultReasonType", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultOfCancelledWithoutHearingResultReasonType() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
        given(hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearingEntity))
            .willReturn(LocalDateTime.of(LocalDate.of(2022, 1, 28), LocalTime.MIN));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual(
                hearingActualsOutcome("CANCELLED", null),
                List.of(actualHearingDay(LocalDate.of(2022, 1, 28)))
            ));
        });
        assertEquals("CANCELLED result requires a hearingResultReasonType", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHearingActualDateInFuture() {
        HearingEntity hearingEntity = mock(HearingEntity.class);
        given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
        given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
        given(hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearingEntity))
            .willReturn(LocalDateTime.of(LocalDate.of(2022, 1, 31), LocalTime.MIN));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual(
                hearingActualsOutcome("COMPLETED", "Nothing more to hear"),
                List.of(actualHearingDay(LocalDate.of(2022, 1, 28)))
            ));
        });
        assertEquals("003 invalid date", exception.getMessage());
    }

}
