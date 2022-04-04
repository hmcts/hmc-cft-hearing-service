package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

class LinkedHearingValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingValidatorTest.class);

    @InjectMocks
    private LinkedHearingValidator linkedHearingValidation;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingValidation =
                new LinkedHearingValidator(hearingRepository, linkedGroupDetailsRepository);
    }

    @Test
    void shouldFailAsRequestIdIsNull() {
        String requestId = null;
        Exception exception = assertThrows(BadRequestException.class, () -> linkedHearingValidation
                .validateRequestId(requestId, null));
        assertEquals(LINKED_GROUP_ID_EMPTY, exception.getMessage());
    }

    @Test
    void shouldFailAsRequestIdDoesNotExist() {
        String requestId = "9176";
        String errorMessage = "This is a test error message";
        when(linkedGroupDetailsRepository.isFoundForRequestId(requestId)).thenReturn(0L);
        Exception exception = assertThrows(LinkedGroupNotFoundException.class, () ->
                linkedHearingValidation.validateRequestId(requestId, errorMessage));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldSucceedAsRequestIdDoesExist() {
        String requestId = "9176";
        String errorMessage = "This is a test error message";
        LinkedGroupDetails lgdExpected = generateLinkedGroupDetails(1L, PutHearingStatus.HEARING_REQUESTED.name(),
                "request1","request 1 in action");
        logger.info("lgdExpected: {}", lgdExpected);
        when(linkedGroupDetailsRepository.isFoundForRequestId(requestId)).thenReturn(1L);
        linkedHearingValidation.validateRequestId(requestId, errorMessage);
    }

    @Test
    void shouldFailAsObsoleteDetailsNotForDeleting() {
        String requestId = "9176";
        String requestName = "Special request";
        final Long hearingId2 = 2000000025L;
        final Long hearingId3 = 2000000026L;
        when(hearingRepository.findByRequestId(requestId)).thenReturn(null);
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(20L, "ACTIVE", requestId, requestName);
        List<HearingEntity> listLinkedHearingDetails = new ArrayList<>();
        HearingEntity hearing2 = generateHearing(hearingId2, PutHearingStatus.HEARING_REQUESTED.name(),
                groupDetails, 2L);
        listLinkedHearingDetails.add(hearing2);
        HearingEntity hearing3 = generateHearing(hearingId3, "NO_DELETE", groupDetails, 3L);
        listLinkedHearingDetails.add(hearing3);

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertFalse(errorMessages.isEmpty());
        assertEquals(1, errorMessages.size());
        assertEquals(INVALID_DELETE_HEARING_GROUP_HEARING_STATUS.replace("%s", hearingId3.toString()),
                errorMessages.get(0));
    }

    @Test
    void shouldSucceedAsObsoleteDetailsAreForDeleting() {
        final String requestId = "9176";
        final String requestName = "Special request";

        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(1L, "ACTIVE", requestId, requestName);
        List<HearingEntity> listLinkedHearingDetails = new ArrayList<>();
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name(),
                groupDetails, 2L);
        listLinkedHearingDetails.add(hearing2);
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name(),
                groupDetails, 3L);
        listLinkedHearingDetails.add(hearing3);

        List<HearingEntity> listObsolete = new ArrayList<>();
        listObsolete.add(listLinkedHearingDetails.get(0));

        when(hearingRepository.findByRequestId(requestId)).thenReturn(listObsolete);

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertTrue(errorMessages.isEmpty());
    }

    @Test
    void shouldExtractObsoleteDetailsForDeleting() {
        final String requestId = "89176";
        final String requestName = "compare and extract obsolete details";
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(1L, "ACTIVE", requestId, requestName);
        List<HearingEntity> existingInDataList =
                generateValidLinkedHearingDetailsList(groupDetails);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "2000000001",
                existingInDataList.get(1).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "2000000003",
                existingInDataList.get(3).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "2000000005",
                existingInDataList.get(5).getLinkedOrder().intValue()));

        List<HearingEntity> listObsoleteDetails =
            linkedHearingValidation.extractObsoleteLinkedHearings(payloadList, existingInDataList);
        assertFalse(listObsoleteDetails.isEmpty());
        assertEquals(3, listObsoleteDetails.size());
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(1)));
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(3)));
        assertTrue(listObsoleteDetails.contains(existingInDataList.get(5)));
    }

    @Test
    void shouldFailWithObsoleteDetailsNotValidForUnlinking() {
        final String requestId = "78176";
        final String requestName = "Find obsolete details ARE NOT not valid for unlinking";
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(1L, "ACTIVE", requestId, requestName);
        List<HearingEntity> existingInDataList =
                generateLinkedHearingDetailsListWithBadStatus(groupDetails);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "2000000007",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "2000000008",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        HearingEntity hearing9 = generateHearing(2000000009L, PutHearingStatus.LISTED.name(), groupDetails, 2L);
        existingInDataList.add(hearing9);

        when(hearingRepository.findByRequestId(requestId))
                .thenReturn(existingInDataList);

        Exception exception = assertThrows(LinkedHearingNotValidForUnlinkingException.class, () ->
                linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList));
        final String expectedErrorMessage = "["
                + "008 Invalid state for unlinking hearing request 2000000009, "
                + "008 Invalid state for unlinking hearing request 2000000011, "
                + "008 Invalid state for unlinking hearing request 2000000012, "
                + "008 Invalid state for unlinking hearing request 2000000013"
                + "]";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void shouldPassWithObsoleteDetailsValidForUnlinking() {
        final String requestId = "181896";
        final String requestName = "Find obsolete details ARE valid for unlinking";
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(1L, "ACTIVE", requestId, requestName);
        HearingEntity hearing9 = generateHearing(20000000009L, PutHearingStatus.LISTED.name(),
                groupDetails, 2L);
        List<HearingEntity> existingInDataList =
                generateValidLinkedHearingDetailsList(groupDetails);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "7",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "8",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        existingInDataList.add(hearing9);

        when(hearingRepository.findByRequestId(requestId))
                .thenReturn(existingInDataList);

        linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList);
    }

    private List<HearingEntity> generateLinkedHearingDetailsListWithBadStatus(LinkedGroupDetails groupDetails) {
        List<HearingEntity> existingInDataList = new ArrayList<>();
        HearingEntity hearing2 = generateHearing(2000000009L, "WRONG_STATUS_2",
                groupDetails, 2L);
        existingInDataList.add(hearing2);
        HearingEntity hearing3 = generateHearing(2000000010L, PutHearingStatus.AWAITING_LISTING.name(),
                groupDetails, 3L);
        existingInDataList.add(hearing3);
        HearingEntity hearing4 = generateHearing(2000000011L, "WRONG_STATUS_2",
                groupDetails, 4L);
        existingInDataList.add(hearing4);
        HearingEntity hearing5 = generateHearing(2000000012L, "WRONG_STATUS_2",
                groupDetails, 5L);
        existingInDataList.add(hearing5);
        HearingEntity hearing6 = generateHearing(2000000013L, "WRONG_STATUS_2",
                groupDetails, 6L);
        existingInDataList.add(hearing6);
        return existingInDataList;
    }

    private List<HearingEntity> generateValidLinkedHearingDetailsList(LinkedGroupDetails groupDetails) {
        List<HearingEntity> existingInDataList = new ArrayList<>();
        HearingEntity hearing1 = generateHearing(2000000001L, PutHearingStatus.AWAITING_LISTING.name(),
                groupDetails, 2L);
        existingInDataList.add(hearing1);
        HearingEntity hearing2 = generateHearing(2000000002L, PutHearingStatus.LISTED.name(),
                groupDetails, 3L);
        existingInDataList.add(hearing2);
        HearingEntity hearing3 = generateHearing(2000000003L, PutHearingStatus.AWAITING_LISTING.name(),
                groupDetails, 4L);
        existingInDataList.add(hearing3);
        HearingEntity hearing4 = generateHearing(2000000004L, PutHearingStatus.HEARING_REQUESTED.name(),
                groupDetails, 5L);
        existingInDataList.add(hearing4);
        HearingEntity hearing5 = generateHearing(2000000005L, PutHearingStatus.AWAITING_LISTING.name(),
                groupDetails, 6L);
        existingInDataList.add(hearing5);
        HearingEntity hearing6 = generateHearing(2000000006L,
                PutHearingStatus.UPDATE_REQUESTED.name(), groupDetails, 7L);
        existingInDataList.add(hearing6);
        return existingInDataList;
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long id, String status,
                                                          String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(id);
        groupDetails.setLinkType(LinkType.SAME_SLOT);
        groupDetails.setReasonForLink("reason for link");
        groupDetails.setRequestDateTime(LocalDateTime.now().plusHours(2));
        groupDetails.setRequestId(requestId);
        groupDetails.setRequestName(requestName);
        groupDetails.setStatus(status);
        return groupDetails;
    }

    private CaseHearingRequestEntity generateCaseHearingRequest(HearingEntity hearing, Integer version,
                                                                LocalDateTime receivedDateTime) {
        CaseHearingRequestEntity request = new CaseHearingRequestEntity();
        request.setHearing(hearing);
        request.setVersionNumber(version);
        request.setHearingRequestReceivedDateTime(receivedDateTime);
        return request;
    }

    private List<CaseHearingRequestEntity> generateCaseHearingRequests(HearingEntity hearing) {
        List<CaseHearingRequestEntity> caseHearingRequests = new ArrayList<>();
        CaseHearingRequestEntity request1 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(1));
        CaseHearingRequestEntity request2 = generateCaseHearingRequest(hearing,
                1, LocalDateTime.now().plusDays(2));
        caseHearingRequests.add(request1);
        caseHearingRequests.add(request2);
        return caseHearingRequests;
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
        hearing.setCaseHearingRequests(generateCaseHearingRequests(hearing));
        return hearing;
    }

    private HearingResponseEntity generateHearingResponse(Long id, LocalDateTime requestTimeStamp,
                                                          Integer responseVersion, HearingEntity hearing) {
        HearingResponseEntity response = new HearingResponseEntity();
        response.setHearingResponseId(id);
        response.setRequestTimeStamp(requestTimeStamp);
        response.setListingCaseStatus(ListingStatus.DRAFT.name());
        response.setResponseVersion(responseVersion);
        response.setHearing(hearing);
        logger.info("response: {}, {}, {}", response.getHearingResponseId(), response.getRequestTimeStamp(),
                response.getResponseVersion());
        return response;
    }

}
