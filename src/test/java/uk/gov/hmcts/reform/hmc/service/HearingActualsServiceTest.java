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
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingActualsMapper;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingAccrualsValidator;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.actualHearingDay;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingActualsOutcome;

@ExtendWith(MockitoExtension.class)
class HearingActualsServiceTest {
    public static final String VALID_HEARING_STAUS = "UPDATE_REQUESTED";
    public static final long INVALID_HEARING_ID = 1000000000L;
    public static final Long HEARING_ID = 2000000000L;

    @InjectMocks
    private HearingActualsServiceImpl hearingActualsService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingEntity hearingEntity;

    @Mock
    private ActualHearingRepository actualHearingRepository;

    @Mock
    private ActualHearingDayRepository actualHearingDayRepository;

    @Mock
    private GetHearingActualsResponseMapper getHearingActualsResponseMapper;

    @Mock
    private HearingResponseRepository hearingResponseRepository;

    @Mock
    HearingAccrualsValidator hearingAccrualsValidatorMock;

    @Mock
    HearingIdValidator hearingIdValidatorMock;

    @Mock
    HearingActualsMapper hearingActualsMapperMock;

    HearingAccrualsValidator hearingAccrualsValidator;

    HearingIdValidator hearingIdValidator;

    HearingActualsMapper hearingActualsMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        HearingActualsMapper hearingActualsMapper = new HearingActualsMapper();
        HearingIdValidator hearingIdValidator = new HearingIdValidator(hearingRepository,
                actualHearingRepository, actualHearingDayRepository);
        HearingAccrualsValidator hearingAccrualsValidator = new HearingAccrualsValidator(hearingIdValidator);
        hearingActualsService =
            new HearingActualsServiceImpl(
                hearingRepository,
                hearingResponseRepository,
                actualHearingRepository,
                getHearingActualsResponseMapper,
                hearingActualsMapper,
                hearingIdValidator,
                hearingAccrualsValidator
            );
    }

    @Nested
    @DisplayName("getHearingActuals")
    class GetHearingActuals {
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
        void shouldPassWithValidHearingIdInDb() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("RESPONDED");
            hearing.setId(2000000000L);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearing));
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            hearingActualsService.getHearingActuals(2000000000L);
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

    @Nested
    @DisplayName("putHearingActuals")
    class PutHearingActuals {
        @BeforeEach
        public void setUp() {
            HearingIdValidator hearingIdValidator = new HearingIdValidator(hearingRepository,
                    actualHearingRepository, actualHearingDayRepository);
            HearingAccrualsValidator hearingAccrualsValidator = new HearingAccrualsValidator(hearingIdValidator);
            HearingActualsMapper hearingActualsMapper = new HearingActualsMapper();
            hearingActualsService =
                new HearingActualsServiceImpl(
                    hearingRepository,
                    hearingResponseRepository,
                    actualHearingRepository,
                    getHearingActualsResponseMapper,
                    hearingActualsMapper,
                    hearingIdValidator,
                    hearingAccrualsValidator
                );
        }

        @Test
        void shouldUpdateHearingActuals() {
            given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
            given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);

            HearingResponseEntity hearingResponseEntityMock = mock(HearingResponseEntity.class);
            given(hearingEntity.getHearingResponseForLatestRequest())
                    .willReturn(Optional.of(hearingResponseEntityMock));

            HearingDayDetailsEntity hearingDayDetailsEntity = mock(HearingDayDetailsEntity.class);
            given(hearingResponseEntityMock.getEarliestHearingDayDetails()).willReturn(
                    Optional.of(hearingDayDetailsEntity));
            given(hearingResponseEntityMock.getEarliestHearingDayDetails().get().getStartDateTime())
                    .willReturn(LocalDateTime.of(2022, 1, 22, 10,30, 00));

            // mock insert
            ActualHearingEntity actualHearingMock = mock(ActualHearingEntity.class);
            HearingActual hearingActual = TestingUtil.hearingActual();

            given(actualHearingRepository.save(any())).willReturn(actualHearingMock);
            given(hearingResponseRepository.save(any())).willReturn(hearingResponseEntityMock);

            assertDoesNotThrow(() -> {
                hearingActualsService.updateHearingActuals(HEARING_ID, hearingActual);
            });
        }

        @Test
        void shouldThrowExceptionWhenInvalidHearingId() {
            // doThrow(new BadRequestException(INVALID_HEARING_ID_DETAILS)).when(hearingIdValidatorMock)
            //      .isValidFormat(anyString());
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
                hearingActualsService.updateHearingActuals(HEARING_ID,
                                                           TestingUtil.hearingActualWithHearingDateInFuture());
            });
            assertEquals("003 invalid date", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenHasHearingResultOfCancelledWithoutHearingResultReasonType() {
            HearingEntity hearingEntity = mock(HearingEntity.class);
            given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
            given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
            HearingResponseEntity hearingResponseEntity = mock(HearingResponseEntity.class);

            given(hearingEntity.getHearingResponseForLatestRequest()).willReturn(Optional.of(hearingResponseEntity));

            HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
            final LocalDateTime earliestHearingDate = LocalDateTime.of(2022, 1, 29, 10,
                    30, 00);
            hearingDayDetailsEntity.setStartDateTime(earliestHearingDate);
            given(hearingResponseEntity.getEarliestHearingDayDetails()).willReturn(
                    Optional.of(hearingDayDetailsEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                hearingActualsService.updateHearingActuals(HEARING_ID, TestingUtil.hearingActual(
                    hearingActualsOutcome("CANCELLED", null),
                    List.of(actualHearingDay(LocalDate.of(2022, 2, 28)))
                ));
            });
            assertTrue(exception.getMessage().contains("CANCELLED result requires a hearingResultReasonType"));
        }

        @Test
        void shouldThrowExceptionWhenHearingActualDateInFuture() {
            HearingEntity hearingEntity = mock(HearingEntity.class);
            final LocalDate lowestStartDate = LocalDate.of(2022, 1, 31);
            final LocalDateTime earliestHearingDate = LocalDateTime.of(2022, 2, 25, 10,
                    30, 00);
            given(hearingEntity.getStatus()).willReturn(VALID_HEARING_STAUS);
            given(hearingRepository.findById(HEARING_ID)).willReturn(Optional.of(hearingEntity));
            HearingResponseEntity hearingResponseEntity = mock(HearingResponseEntity.class);
            given(hearingEntity.getHearingResponseForLatestRequest()).willReturn(Optional.of(hearingResponseEntity));
            HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
            hearingDayDetailsEntity.setStartDateTime(earliestHearingDate);
            given(hearingResponseEntity.getEarliestHearingDayDetails()).willReturn(
                    Optional.of(hearingDayDetailsEntity));
            HearingActual hearingActual =  TestingUtil.hearingActual(
                    hearingActualsOutcome("COMPLETED", "Nothing more to hear"),
                    List.of(actualHearingDay(lowestStartDate)));


            Exception exception = assertThrows(BadRequestException.class, () -> {
                hearingActualsService.updateHearingActuals(HEARING_ID, hearingActual);
            });
            assertEquals("003 invalid date", exception.getMessage());
        }

    }
}
