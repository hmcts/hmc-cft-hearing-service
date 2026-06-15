package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_FINAL_FLAG_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_REQUEST_DATE_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID;

class HearingActualsValidatorTest {

    @InjectMocks
    private HearingIdValidator hearingIdValidator;

    @InjectMocks
    private HearingActualsValidator hearingActualsValidator;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ActualHearingRepository actualHearingRepository;

    private static final Long VALID_HEARING_ID = 2000000000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingActualsValidator = new HearingActualsValidator(hearingIdValidator);
    }

    @Test
    void testExpectedException_BadOutcomeInfo() {
        HearingEntity expectedHearing = generateHearing(VALID_HEARING_ID,
                PutHearingStatus.HEARING_REQUESTED.name(),
                null, 1L);
        expectedHearing.setCaseHearingRequests(generateCaseHearingRequests(expectedHearing));
        when(hearingRepository.findById(VALID_HEARING_ID)).thenReturn(Optional.of(expectedHearing));
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingActualsValidator
                .validateHearingOutcomeInformation(VALID_HEARING_ID));
        assertTrue(exception.getMessage().contains(ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME));
    }

    @Test
    void testNoException_BadOutcomeInfoIsGood() {
        HearingEntity expectedHearing = generateHearing(VALID_HEARING_ID,
                PutHearingStatus.HEARING_REQUESTED.name(),
                null, 1L);
        expectedHearing.setCaseHearingRequests(generateCaseHearingRequests(expectedHearing));
        Optional<ActualHearingEntity> actualHearing = Optional.of(generateActualHearing(VALID_HEARING_ID));
        generateHearingResponseEntity(1, expectedHearing,
                actualHearing.get());
        when(hearingRepository.findById(VALID_HEARING_ID)).thenReturn(Optional.of(expectedHearing));
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        when(actualHearingRepository.findByHearingResponse(any())).thenReturn(actualHearing);
        hearingActualsValidator
                .validateHearingOutcomeInformation(VALID_HEARING_ID);
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultTypeIsNull() {
        HearingResultType hearingResultType = null;
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResult(hearingResultType);
        });
        assertTrue(exception.getMessage().contains(HA_OUTCOME_RESULT_NOT_EMPTY));
    }

    @Test
    void shouldNotThrowExceptionWhenHasHearingResultTypeIsNotNull() {
        HearingResultType hearingResultType = HearingResultType.ADJOURNED;
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResult(hearingResultType);
        });
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultStringIsNull() {
        String hearingResult = null;
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResult(hearingResult);
        });
        assertTrue(exception.getMessage().contains(HA_OUTCOME_RESULT_NOT_EMPTY));
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultStringIsInvalid() {
        String hearingResult = "INVALID";
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResult(hearingResult);
        });
        assertTrue(exception.getMessage().contains(HA_OUTCOME_RESULT_NOT_EMPTY));
    }

    @Test
    void shouldNotErrorWhenHasHearingResultStringIsAdjourned() {
        String hearingResult = HearingResultType.ADJOURNED.getLabel();
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResult(hearingResult);
        });
    }

    @Test
    void shouldNotErrorWhenHasHearingResultStringIsCancelled() {
        String hearingResult = HearingResultType.CANCELLED.getLabel();
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResult(hearingResult);
        });
    }

    @Test
    void shouldNotErrorWhenHasHearingResultStringIsCompleted() {
        String hearingResult = HearingResultType.COMPLETED.getLabel();
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResult(hearingResult);
        });
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultTypeIsAdjournedAndNoHearingResultReasonType() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResult(HearingResultType.ADJOURNED, null);
        });
        assertTrue(exception.getMessage().contains("ADJOURNED result requires a hearingResultReasonType"));
    }

    @Test
    void shouldNotThrowExceptionWhenHasHearingResultTypeIsNullAndNoHearingResultReasonType() {
        HearingResultType hearingResultType = null;
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResult(hearingResultType, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenHasHearingResultOfAdjournedWithoutHearingResultReasonType() {

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResult("ADJOURNED", null);
        });
        assertTrue(exception.getMessage().contains("ADJOURNED result requires a hearingResultReasonType"));
    }

    @Test
    void shouldNotErrorWhenHasValidHearingIsFinalFlag() {
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateActualHearingIsFinalFlag(Boolean.TRUE);
        });
    }

    @Test
    void shouldThrowExceptionWhenHasNullHearingIsFinalFlag() {

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateActualHearingIsFinalFlag(null);
        });
        assertTrue(exception.getMessage().contains(HA_OUTCOME_FINAL_FLAG_NOT_EMPTY));
    }

    @Test
    void shouldNotErrorWhenHasHearingResultDateIsToday() {
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResultDate(LocalDate.now());
        });
    }

    @Test
    void shouldNotErrorWhenHasHearingResultDateIsThreeDaysAgo() {
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingResultDate(LocalDate.now().minusDays(3));
        });
    }

    @Test
    void shouldThrowErrorWhenHasHearingResultDateIsNull() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingActualsValidator.validateHearingResultDate(null);
        });
        assertTrue(exception.getMessage().contains(HA_OUTCOME_REQUEST_DATE_NOT_EMPTY));
    }

    @Test
    void hearingDate_Future_Outcome_Empty_NotRequired_Default() {
        HearingActual actual = TestingUtil.hearingActualOutcomeAndActualHearingDaysNull();
        assertDoesNotThrow(() -> {
            hearingActualsValidator.validateHearingActualDaysNotInTheFuture(actual);
        });
    }

    @Test
    void validateHearingActualDaysNotInTheFuture_NotRequiredShouldBeFalseWhenSetToNull() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        LocalDateTime futureStartTime = LocalDateTime.of(futureDate, LocalTime.of(13, 0, 0));
        LocalDateTime futureEndTime = LocalDateTime.of(futureDate, LocalTime.of(14, 0, 0));

        ActualHearingDay actualHearingDay = createActualHearingDay(futureDate, null, futureStartTime, futureEndTime);
        HearingActual hearingActual = new HearingActual();
        hearingActual.setActualHearingDays(List.of(actualHearingDay));

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> hearingActualsValidator.validateHearingActualDaysNotInTheFuture(hearingActual),
                         "BadRequestException should be thrown");

        assertEquals(HEARING_ACTUALS_HEARING_DAYS_INVALID,
                     exception.getMessage(),
                     "BadRequestException has unexpected message");
    }

    @ParameterizedTest
    @MethodSource("actualHearingDayAccepted")
    void validateHearingActualDaysNotInTheFuture_ShouldAcceptActualHearingDay(ActualHearingDay actualHearingDay) {
        HearingActual hearingActual = new HearingActual();
        hearingActual.setActualHearingDays(List.of(actualHearingDay));

        assertDoesNotThrow(() -> hearingActualsValidator.validateHearingActualDaysNotInTheFuture(hearingActual),
                           "Exception should not be thrown");
    }

    @ParameterizedTest
    @MethodSource("actualHearingDayRejected")
    void validateHearingActualDaysNotInTheFuture_ShouldRejectActualHearingDay(ActualHearingDay actualHearingDay) {
        HearingActual hearingActual = new HearingActual();
        hearingActual.setActualHearingDays(List.of(actualHearingDay));

        BadRequestException exception =
            assertThrows(BadRequestException.class,
                         () -> hearingActualsValidator.validateHearingActualDaysNotInTheFuture(hearingActual),
                         "BadRequestException should be thrown");

        assertEquals(HEARING_ACTUALS_HEARING_DAYS_INVALID,
                     exception.getMessage(),
                     "BadRequestException has unexpected message");
    }

    private static Stream<Arguments> actualHearingDayAccepted() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentStartTime = LocalDateTime.of(currentDate, LocalTime.of(13, 0, 0));
        LocalDateTime currentEndTime = LocalDateTime.of(currentDate, LocalTime.of(14, 0, 0));

        ActualHearingDay dayCurrentRequiredEmpty = createActualHearingDay(currentDate, false);
        ActualHearingDay dayCurrentRequiredNotEmpty =
            createActualHearingDay(currentDate, false, currentStartTime, currentEndTime);

        ActualHearingDay dayCurrentNotRequiredEmpty = createActualHearingDay(currentDate, true);
        ActualHearingDay dayCurrentNotRequiredNotEmpty =
            createActualHearingDay(currentDate, true, currentStartTime, currentEndTime);

        LocalDate futureDate = LocalDate.now().plusDays(1);

        ActualHearingDay dayFutureRequiredEmpty = createActualHearingDay(futureDate, false);
        ActualHearingDay dayFutureNotRequiredEmpty = createActualHearingDay(futureDate, true);

        return Stream.of(
            arguments(dayCurrentRequiredEmpty),
            arguments(dayCurrentRequiredNotEmpty),
            arguments(dayCurrentNotRequiredEmpty),
            arguments(dayCurrentNotRequiredNotEmpty),
            arguments(dayFutureRequiredEmpty),
            arguments(dayFutureNotRequiredEmpty)
        );
    }

    private static Stream<Arguments> actualHearingDayRejected() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        LocalDateTime futureStartTime = LocalDateTime.of(futureDate, LocalTime.of(13, 0, 0));
        LocalDateTime futureEndTime = LocalDateTime.of(futureDate, LocalTime.of(14, 0, 0));

        ActualHearingDay dayFutureRequiredNotEmpty =
            createActualHearingDay(futureDate, false, futureStartTime, futureEndTime);

        ActualHearingDay dayFutureNotRequiredNotEmpty =
            createActualHearingDay(futureDate, true, futureStartTime, futureEndTime);

        return Stream.of(
            arguments(dayFutureRequiredNotEmpty),
            arguments(dayFutureNotRequiredNotEmpty)
        );
    }

    private static ActualHearingDay createActualHearingDay(LocalDate hearingDate, Boolean notRequired) {
        ActualHearingDay actualHearingDay = new ActualHearingDay();
        actualHearingDay.setHearingDate(hearingDate);
        actualHearingDay.setNotRequired(notRequired);

        return actualHearingDay;
    }

    private static ActualHearingDay createActualHearingDay(LocalDate hearingDate,
                                                           Boolean notRequired,
                                                           LocalDateTime hearingStartTime,
                                                           LocalDateTime hearingEndTime) {
        ActualHearingDay actualHearingDay = createActualHearingDay(hearingDate, notRequired);
        actualHearingDay.setHearingStartTime(hearingStartTime);
        actualHearingDay.setHearingEndTime(hearingEndTime);

        return actualHearingDay;
    }

    protected void generateHearingResponseEntity(Integer requestVersion, HearingEntity hearingEntity,
                                               ActualHearingEntity actualHearingEntity) {
        HearingResponseEntity responseEntity = new HearingResponseEntity();
        responseEntity.setCancellationReasonType("Test Reason Type");
        responseEntity.setRequestVersion(requestVersion);
        responseEntity.setHearing(hearingEntity);
        responseEntity.setActualHearingEntity(actualHearingEntity);
        hearingEntity.setHearingResponses(List.of(responseEntity));
        actualHearingEntity.setHearingResponse(responseEntity);
    }

    protected ActualHearingEntity generateActualHearing(Long id) {
        ActualHearingEntity actualHearing = new ActualHearingEntity();
        actualHearing.setActualHearingId(id);
        actualHearing.setActualHearingIsFinalFlag(true);
        actualHearing.setActualHearingType("Test 111");
        actualHearing.setHearingResultType(HearingResultType.ADJOURNED);
        actualHearing.setHearingResultReasonType("REASON TYPE 78");
        actualHearing.setHearingResultDate(LocalDate.now().minusDays(7));
        return actualHearing;
    }

    protected List<CaseHearingRequestEntity> generateCaseHearingRequests(HearingEntity hearing) {
        List<CaseHearingRequestEntity> caseHearingRequests = new ArrayList<>();
        CaseHearingRequestEntity request1 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(1));
        CaseHearingRequestEntity request2 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(2));
        caseHearingRequests.add(request1);
        caseHearingRequests.add(request2);
        return caseHearingRequests;
    }

    protected HearingEntity generateHearing(Long id, String status, LinkedGroupDetails groupDetails, Long linkedOrder) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(id);
        hearing.setStatus(status);
        hearing.setLinkedGroupDetails(groupDetails);
        if (null == groupDetails) {
            hearing.setIsLinkedFlag(true);
        }
        hearing.setLinkedOrder(linkedOrder);
        return hearing;
    }

    protected CaseHearingRequestEntity generateCaseHearingRequest(HearingEntity hearing, Integer version,
                                                                LocalDateTime receivedDateTime) {
        CaseHearingRequestEntity request = new CaseHearingRequestEntity();
        request.setHearing(hearing);
        request.setVersionNumber(version);
        request.setHearingRequestReceivedDateTime(receivedDateTime);
        return request;
    }
}
