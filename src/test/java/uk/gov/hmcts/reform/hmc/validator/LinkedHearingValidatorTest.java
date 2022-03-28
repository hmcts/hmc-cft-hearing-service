package uk.gov.hmcts.reform.hmc.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

class LinkedHearingValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHearingValidatorTest.class);

    @InjectMocks
    private LinkedHearingValidator linkedHearingValidation;

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
                new LinkedHearingValidator(hearingRepository, linkedGroupDetailsRepository,
                        linkedHearingDetailsRepository);
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
        when(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId)).thenReturn(null);
        List<LinkedHearingDetailsAudit> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId, requestName, 20L);
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
        final String requestId = "9176";
        final String requestName = "Special request";

        List<LinkedHearingDetailsAudit> listLinkedHearingDetails =
                generateLinkedHearingDetailsList(requestId.toString(), requestName, 10L);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.HEARING_REQUESTED.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(2L, hearing2,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        listLinkedHearingDetails.add(generateLinkedHearingDetails(3L, hearing3,
                listLinkedHearingDetails.get(0).getLinkedGroup(), 3L));

        List<LinkedHearingDetailsAudit> listObsolete = new ArrayList<>();
        listObsolete.add(listLinkedHearingDetails.get(0));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId)).thenReturn(listObsolete);

        List<String> errorMessages = linkedHearingValidation.validateObsoleteLinkedHearings(listLinkedHearingDetails);
        assertTrue(errorMessages.isEmpty());
    }

    @Test
    void shouldExtractObsoleteDetailsForDeleting() {
        final String requestId = "89176";
        final String requestName = "compare and extract obsolete details";
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateValidLinkedHearingDetailsList(requestId, requestName, 7L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "1",
                existingInDataList.get(1).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "3",
                existingInDataList.get(3).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "5",
                existingInDataList.get(5).getLinkedOrder().intValue()));

        List<LinkedHearingDetailsAudit> listObsoleteDetails =
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
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateLinkedHearingDetailsListWithBadStatus(requestId, requestName, 8L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "7",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
                "8",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        HearingEntity hearing9 = generateHearing(9L, PutHearingStatus.LISTED.name());
        existingInDataList.add(generateLinkedHearingDetails(9L, hearing9,
                existingInDataList.get(0).getLinkedGroup(), 9L));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId))
                .thenReturn(existingInDataList);

        Exception exception = assertThrows(LinkedHearingNotValidForUnlinkingException.class, () ->
                linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList));
        final String expectedErrorMessage = "["
                + "008 Invalid state for unlinking hearing request 2, "
                + "008 Invalid state for unlinking hearing request 4, "
                + "008 Invalid state for unlinking hearing request 5, "
                + "008 Invalid state for unlinking hearing request 6"
                + "]";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }


    @Test
    void shouldPassWithObsoleteDetailsValidForUnlinking() {
        final String requestId = "181896";
        final String requestName = "Find obsolete details ARE valid for unlinking";
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateValidLinkedHearingDetailsList(requestId, requestName, 9L);

        List<LinkHearingDetails> payloadList = new ArrayList<>();
        payloadList.add(new LinkHearingDetails(
                "7",
                existingInDataList.get(0).getLinkedOrder().intValue()));
        payloadList.add(new LinkHearingDetails(
            "8",
            existingInDataList.get(0).getLinkedOrder().intValue()
        ));
        HearingEntity hearing9 = generateHearing(9L, PutHearingStatus.LISTED.name());
        existingInDataList.add(generateLinkedHearingDetails(9L, hearing9,
                                                            existingInDataList.get(0).getLinkedGroup(), 9L
        ));

        when(linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId))
            .thenReturn(existingInDataList);

        linkedHearingValidation.validateLinkedHearingsForUpdate(requestId, payloadList);
    }

    @Test
    void shouldPassWithDetailsValidForUpdateHearingWithLinkGroup() {
        LinkHearingDetails hearingInGroup = new LinkHearingDetails();
        hearingInGroup.setHearingId("2000000000");
        hearingInGroup.setHearingOrder(1);

        LinkHearingDetails hearingInGroup1 = new LinkHearingDetails();
        hearingInGroup1.setHearingId("2000000002");
        hearingInGroup1.setHearingOrder(2);

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setGroupDetails(generateGroupDetails(LinkType.SAME_SLOT));
        hearingLinkGroupRequest.setHearingsInGroup(Arrays.asList(hearingInGroup, hearingInGroup1));

        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(
            TestingUtil.hearingEntityWithLinkDetails()));

        when(hearingRepository.existsById(2000000002L)).thenReturn(true);
        when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(
            TestingUtil.hearingEntityWithLinkDetails()));

        given(hearingRepository.save(any())).willReturn(TestingUtil.hearingEntity());
        given(linkedGroupDetailsRepository.save(any())).willReturn(TestingUtil.linkedGroupDetailsEntity());

        linkedHearingValidation.updateHearingWithLinkGroup(hearingLinkGroupRequest);
        verify(hearingRepository, times(1)).findById(2000000000L);
        verify(hearingRepository, times(1)).findById(2000000002L);
        verify(hearingRepository, times(2)).save(any());
        verify(linkedGroupDetailsRepository, times(1)).save(any());
    }

    private List<LinkedHearingDetailsAudit> generateLinkedHearingDetailsListWithBadStatus(
        String requestId, String requestName, Long groupId) {

        List<LinkedHearingDetailsAudit> existingInDataList =
            generateLinkedHearingDetailsList(requestId, requestName, groupId);
        HearingEntity hearing2 = generateHearing(2L, "WRONG_STATUS_2");
        existingInDataList.add(generateLinkedHearingDetails(2L, hearing2,
                                                            existingInDataList.get(0).getLinkedGroup(), 2L
        ));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(3L, hearing3,
                existingInDataList.get(0).getLinkedGroup(), 3L));
        HearingEntity hearing4 = generateHearing(4L, "WRONG_STATUS_4");
        existingInDataList.add(generateLinkedHearingDetails(4L, hearing4,
                existingInDataList.get(0).getLinkedGroup(), 4L));
        HearingEntity hearing5 = generateHearing(5L, "WRONG_STATUS_5");
        existingInDataList.add(generateLinkedHearingDetails(5L, hearing5,
                existingInDataList.get(0).getLinkedGroup(), 5L));
        HearingEntity hearing6 = generateHearing(6L, "WRONG_STATUS_6");
        existingInDataList.add(generateLinkedHearingDetails(6L, hearing6,
                existingInDataList.get(0).getLinkedGroup(), 6L));
        return existingInDataList;
    }

    private List<LinkedHearingDetailsAudit> generateValidLinkedHearingDetailsList(String requestId, String requestName,
                                                                                  Long groupId) {
        List<LinkedHearingDetailsAudit> existingInDataList =
                generateLinkedHearingDetailsList(requestId, requestName, groupId);
        HearingEntity hearing2 = generateHearing(2L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(2L, hearing2,
                existingInDataList.get(0).getLinkedGroup(), 2L));
        HearingEntity hearing3 = generateHearing(3L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(3L, hearing3,
                existingInDataList.get(0).getLinkedGroup(), 3L));
        HearingEntity hearing4 = generateHearing(4L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(4L, hearing4,
                existingInDataList.get(0).getLinkedGroup(), 4L));
        HearingEntity hearing5 = generateHearing(5L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(5L, hearing5,
                existingInDataList.get(0).getLinkedGroup(), 5L));
        HearingEntity hearing6 = generateHearing(6L, PutHearingStatus.AWAITING_LISTING.name());
        existingInDataList.add(generateLinkedHearingDetails(6L, hearing6,
                existingInDataList.get(0).getLinkedGroup(), 6L));
        return existingInDataList;
    }



    private List<LinkedHearingDetailsAudit> generateLinkedHearingDetailsList(String requestId,
                                                                             String requestName, Long groupId) {
        HearingEntity hearing = generateHearing(1L, PutHearingStatus.HEARING_REQUESTED.name());
        LinkedGroupDetails groupDetails = generateLinkedGroupDetails(
                groupId, PutHearingStatus.HEARING_REQUESTED.name(), requestId, requestName);
        List<LinkedHearingDetailsAudit> linkedHearingDetailsList = new ArrayList<>();
        linkedHearingDetailsList.add(generateLinkedHearingDetails(1L, hearing, groupDetails, 1L));
        return linkedHearingDetailsList;
    }

    private LinkedGroupDetails generateLinkedGroupDetails(Long id, String status,
                                                          String requestId, String requestName) {
        LinkedGroupDetails groupDetails = new LinkedGroupDetails();
        groupDetails.setLinkedGroupId(id);
        groupDetails.setLinkType(LinkType.SAME_SLOT);
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

    private LinkedHearingDetailsAudit generateLinkedHearingDetails(Long id, HearingEntity hearing,
                                                                   LinkedGroupDetails groupDetails, Long linkedOrder) {
        LinkedHearingDetailsAudit linkedHearingDetails = new LinkedHearingDetailsAudit();
        linkedHearingDetails.setHearing(hearing);
        linkedHearingDetails.setLinkedHearingDetailsAuditId(id);
        linkedHearingDetails.setLinkedGroup(groupDetails);
        linkedHearingDetails.setLinkedOrder(linkedOrder);
        return linkedHearingDetails;
    }

    private GroupDetails generateGroupDetails(LinkType linkType) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupComments("comments");
        groupDetails.setGroupLinkType(linkType.label);
        groupDetails.setGroupName("name");
        groupDetails.setGroupReason("reason");
        return groupDetails;
    }

}
