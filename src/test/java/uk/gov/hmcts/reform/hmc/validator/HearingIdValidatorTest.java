package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

class HearingIdValidatorTest {

    private static final Long VALID_HEARING_ID = 2000000000L;

    @InjectMocks
    private HearingIdValidator hearingIdValidator;

    @Mock
    private HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingIdValidator =
                new HearingIdValidator(hearingRepository);
    }

    @Test
    void shouldGetHearing() {
        HearingEntity expectedHearing = generateHearing(VALID_HEARING_ID,
                PutHearingStatus.HEARING_REQUESTED.name(),
                null, 1L);
        when(hearingRepository.findById(VALID_HEARING_ID)).thenReturn(Optional.of(expectedHearing));
        Optional<HearingEntity> hearing = hearingIdValidator.getHearing(VALID_HEARING_ID);
        assertEquals(expectedHearing, hearing.get());
    }

    @Test
    void shouldFailAsNullInvalidHearingIdFormat() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(null, null));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void shouldFailAsMaxLengthInvalidHearingIdFormat() {
        final Long hearingId = 1000000000000000000L;
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(hearingId, INVALID_HEARING_ID_DETAILS));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void shouldFailAsWrongNumberInvalidHearingIdFormat() {
        final Long hearingId = 1000000000L;
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(hearingId, INVALID_HEARING_ID_DETAILS));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void shouldFailAsAlphamericInvalidHearingIdFormat() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .isValidFormat("ABCDEFG"));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void shouldSucceedAsValidHearingIdFormat() {
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        hearingIdValidator.validateHearingId(VALID_HEARING_ID, null);
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdNotPresentInDB() {
        final String expectedErrorMsg = "Whatever error message";
        when(hearingRepository.existsById(any())).thenReturn(false);
        Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingIdValidator
                .validateHearingId(2000000000L, expectedErrorMsg));
        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdDoesNotStartWith2() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(1000000100L, INVALID_HEARING_ID_DETAILS));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdExceedsMaxLength() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(20000000001111L, INVALID_HEARING_ID_DETAILS));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdIsNull() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(null, INVALID_HEARING_ID_DETAILS));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_Exceeds_MaxLength() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(20000000001111L, "Invalid hearing Id"));
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_First_Char_Is_Not_2() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(1000000100L, "Invalid hearing Id"));
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    private HearingEntity generateHearing(Long id, String status, LinkedGroupDetails groupDetails, Long linkedOrder) {
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

}
