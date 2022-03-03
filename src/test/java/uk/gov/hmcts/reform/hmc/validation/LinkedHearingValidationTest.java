package uk.gov.hmcts.reform.hmc.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

class LinkedHearingValidationTest {

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
                new LinkedHearingValidation(hearingRepository, linkedGroupDetailsRepository,
                        linkedHearingDetailsRepository);
    }

    @Test
    void shouldFailAsRequestIdIsNull() {
        Long requestId = null;
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingValidation
                .validateRequestId(requestId, null));
        assertEquals(LINKED_GROUP_ID_EMPTY, exception.getMessage());
    }

    @Test
    void shouldFailAsRequestIdDoesNotExist() {
        Long requestId = 9176L;
        String errorMessage = "This is a test error message";
        when(linkedGroupDetailsRepository.existsById(requestId)).thenReturn(false);

        Exception exception = assertThrows(LinkedGroupNotFoundException.class, () -> linkedHearingValidation
                .validateRequestId(requestId, errorMessage));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldSucceedAsRequestIdDoesExist() {
        Long requestId = 9176L;
        String errorMessage = "This is a test error message";
        when(linkedGroupDetailsRepository.existsById(requestId)).thenReturn(true);
        linkedHearingValidation.validateRequestId(requestId, errorMessage);
    }

    @Test
    void shouldFailAsObsoleteDetailsNotForDeleting() {
        Long requestId = 9176L;
        String requestName = "Special request";
        when(linkedGroupDetailsRepository.existsById(requestId)).thenReturn(true);
        List<LinkedHearingDetails> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId.toString(), requestName, 20L);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(2L, hearing2,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(2L, "NO_DELETE");
        listLinkedHearingDetails.add(generateLinkedHearingDetails(3L, hearing3,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 3L));

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertFalse(errorMessages.isEmpty());
        assertEquals(1, errorMessages.size());
        assertEquals("008 Invalid state for unlinking hearing request 2", errorMessages.get(0));
    }

    @Test
    void shouldSucceedAsObsoleteDetailsAreForDeleting() {
        Long requestId = 9176L;
        String requestName = "Special request";
        when(linkedGroupDetailsRepository.existsById(requestId)).thenReturn(true);
        List<LinkedHearingDetails> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId.toString(), requestName, 9L);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(2L, hearing2,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(3L, hearing3,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 3L));

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertTrue(errorMessages.isEmpty());
    }


    private List<LinkedHearingDetails> generateLinkedHearingDetailsList(String requestId, String requestName, Long groupId) {
        HearingEntity hearing = generateHearing(1L, PutHearingStatus.HEARING_REQUESTED.name());
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(
                groupId, PutHearingStatus.HEARING_REQUESTED.name(), requestId, requestName);
        List<LinkedHearingDetails> linkedHearingDetailsList = new ArrayList<>();
        linkedHearingDetailsList.add(generateLinkedHearingDetails(1L, hearing, groupDetails, 2L));
        return linkedHearingDetailsList;
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long id, String status, String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(id);
        groupDetails.setLinkType("link type");
        groupDetails.setReasonForLink("reason for link");
        groupDetails.setRequestDateTime(LocalDateTime.now());
        groupDetails.setRequestId(requestId);
        groupDetails.setRequestName(requestName);
        groupDetails.setStatus(status);
        return groupDetails;
    }

    private HearingEntity generateHearing(Long id, String status) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(id);
        hearing.setStatus(status);
        return hearing;
    }

    private LinkedHearingDetails generateLinkedHearingDetails(Long id, HearingEntity hearing,
                                                              LinkedGroupDetails groupDetails, Long linkedOrder) {
        LinkedHearingDetails linkedHearingDetails = new LinkedHearingDetails();
        linkedHearingDetails.setHearing(hearing);
        linkedHearingDetails.setLinkedHearingId(id);
        linkedHearingDetails.setLinkedGroup(groupDetails);
        linkedHearingDetails.setLinkedOrder(linkedOrder);
        return linkedHearingDetails;
    }

}
