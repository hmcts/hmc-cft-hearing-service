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
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

class HearingIdValidatorTest {

    private static final Long VALID_HEARING_ID = 2000000000L;
    private static final Long INVALID_HEARING_ID = 1000000000L;

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
    void shouldFailAsInvalidHearingIdFormat() {
        Exception exception = assertThrows(BadRequestException.class, () -> hearingIdValidator
                .validateHearingId(INVALID_HEARING_ID, null));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void shouldSucceedAsValidHearingIdFormat() {
        when(hearingRepository.existsById(VALID_HEARING_ID)).thenReturn(true);
        hearingIdValidator.validateHearingId(VALID_HEARING_ID, null);
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
