package uk.gov.hmcts.reform.hmc.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

public class LinkedHearingValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingValidationTest.class);

    @InjectMocks
    private LinkedHearingValidation linkedHearingValidation;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    private LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingValidation =
                new LinkedHearingValidation(hearingRepository,
                                            linkedGroupDetailsRepository,
                                            linkedHearingDetailsRepository);
    }
    @Test
    void shouldFailAsRequestIdIsNull() {
        Long requestId = null;
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingValidation
                .validateRequestId(requestId, null));
        assertEquals(LINKED_GROUP_ID_EMPTY, exception.getMessage());
    }

}
