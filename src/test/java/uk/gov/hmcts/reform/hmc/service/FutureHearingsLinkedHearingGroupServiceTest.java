package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.PreviousLinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FutureHearingsLinkedHearingGroupServiceTest {

    private static final long LINKED_GROUP_ID = 100L;
    private static final String REQUEST_ID = "1000";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PENDING = "PENDING";
    private static final LocalDateTime ORIGINAL_REQUEST_DATE_TIME = LocalDateTime.of(2025, Month.NOVEMBER, 1, 12, 0, 0);
    private static final LocalDateTime NEW_REQUEST_DATE_TIME = LocalDateTime.of(2025, Month.NOVEMBER, 2, 13, 0, 0);

    private static final long HEARING_ID_ONE = 2000000000L;
    private static final long HEARING_ID_TWO = 2000000001L;
    private static final long HEARING_ID_THREE = 2000000002L;
    private static final long HEARING_ID_FOUR = 2000000003L;

    @Mock
    private HearingRepository mockHearingRepository;

    @Mock
    private LinkedGroupDetailsRepository mockLinkedGroupDetailsRepository;

    @Mock
    private LinkedHearingValidator mockLinkedHearingValidator;

    @Mock
    private LinkedHearingDetailsAuditRepository mockLinkedHearingDetailsAuditRepository;

    @Mock
    private LinkedGroupDetailsAuditRepository mockLinkedGroupDetailsAuditRepository;

    @Mock
    private LinkedGroupDetailsAuditMapper mockLinkedGroupDetailsAuditMapper;

    @Mock
    private LinkedHearingDetailsAuditMapper mockLinkedHearingDetailsAuditMapper;

    @Mock
    private AccessControlService mockAccessControlService;

    private FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService;

    @BeforeEach
    void setUp() {
        futureHearingsLinkedHearingGroupService =
            new FutureHearingsLinkedHearingGroupService(mockHearingRepository,
                                                        mockLinkedGroupDetailsRepository,
                                                        mockLinkedHearingValidator,
                                                        mockLinkedHearingDetailsAuditRepository,
                                                        mockLinkedGroupDetailsAuditRepository,
                                                        mockLinkedGroupDetailsAuditMapper,
                                                        mockLinkedHearingDetailsAuditMapper,
                                                        mockAccessControlService);
    }

    @Test
    void processAmendLinkedHearingRequest_shouldUnlinkExistingHearings() {
        LinkedGroupDetails currentLinkedGroupDetails =
            createLinkedGroupDetails("name", "GR", STATUS_ACTIVE, "comment", 1L, ORIGINAL_REQUEST_DATE_TIME);

        HearingEntity hearingOne = createHearing(HEARING_ID_ONE, currentLinkedGroupDetails, 1L);
        HearingEntity hearingTwo = createHearing(HEARING_ID_TWO, currentLinkedGroupDetails, 2L);
        final List<HearingEntity> currentHearings = List.of(hearingOne, hearingTwo);

        LinkHearingDetails linkHearingDetailsHearingThree = new LinkHearingDetails(String.valueOf(HEARING_ID_THREE), 1);
        LinkHearingDetails linkHearingDetailsHearingFour = new LinkHearingDetails(String.valueOf(HEARING_ID_FOUR), 2);
        List<LinkHearingDetails> hearingsInGroup =
            List.of(linkHearingDetailsHearingThree, linkHearingDetailsHearingFour);

        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        HearingEntity hearingThree = createHearing(HEARING_ID_THREE);
        HearingEntity hearingFour = createHearing(HEARING_ID_FOUR);

        Optional<HearingEntity> hearingThreeOptional = Optional.of(hearingThree);
        Optional<HearingEntity> hearingFourOptional = Optional.of(hearingFour);

        LinkedHearingDetailsAudit linkedHearingDetailsAuditHearingThree =
            createLinkedHearingDetailsAudit(currentLinkedGroupDetails, hearingThree, 1L);

        LinkedHearingDetailsAudit linkedHearingDetailsAuditHearingFour =
            createLinkedHearingDetailsAudit(currentLinkedGroupDetails, hearingFour, 2L);

        LinkedGroupDetailsAudit linkedGroupDetailsAudit = createLinkedGroupDetailsAudit(currentLinkedGroupDetails);

        when(mockLinkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
            .thenReturn(currentLinkedGroupDetails);
        when(mockLinkedGroupDetailsRepository.save(currentLinkedGroupDetails)).thenReturn(currentLinkedGroupDetails);

        when(mockHearingRepository.existsById(HEARING_ID_THREE)).thenReturn(true);
        when(mockHearingRepository.existsById(HEARING_ID_FOUR)).thenReturn(true);
        when(mockLinkedHearingValidator.getHearingOrder(linkHearingDetailsHearingThree, hearingLinkGroupRequest))
            .thenReturn(1L);
        when(mockLinkedHearingValidator.getHearingOrder(linkHearingDetailsHearingFour, hearingLinkGroupRequest))
            .thenReturn(2L);

        when(mockLinkedGroupDetailsAuditMapper.modelToEntity(currentLinkedGroupDetails))
            .thenReturn(linkedGroupDetailsAudit);

        when(mockHearingRepository.findById(HEARING_ID_THREE)).thenReturn(hearingThreeOptional);
        when(mockHearingRepository.findById(HEARING_ID_FOUR)).thenReturn(hearingFourOptional);

        when(mockLinkedHearingDetailsAuditMapper.modelToEntity(hearingThree, currentLinkedGroupDetails))
            .thenReturn(linkedHearingDetailsAuditHearingThree);
        when(mockLinkedHearingDetailsAuditMapper.modelToEntity(hearingFour, currentLinkedGroupDetails))
            .thenReturn(linkedHearingDetailsAuditHearingFour);

        futureHearingsLinkedHearingGroupService.processAmendLinkedHearingRequest(hearingLinkGroupRequest,
                                                                                 currentHearings,
                                                                                 REQUEST_ID);

        assertHearingHasNoLinkGroupAndOrder(hearingOne);
        assertHearingHasNoLinkGroupAndOrder(hearingTwo);

        assertLinkedGroupDetailsUpdated(currentLinkedGroupDetails);

        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_ONE);
        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_TWO);

        verify(mockLinkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
        verify(mockLinkedGroupDetailsRepository).save(currentLinkedGroupDetails);

        verify(mockHearingRepository).existsById(HEARING_ID_THREE);
        verify(mockHearingRepository).existsById(HEARING_ID_FOUR);
        verify(mockLinkedHearingValidator).getHearingOrder(linkHearingDetailsHearingThree, hearingLinkGroupRequest);
        verify(mockLinkedHearingValidator).getHearingOrder(linkHearingDetailsHearingFour, hearingLinkGroupRequest);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_THREE, currentLinkedGroupDetails, 1L);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_FOUR, currentLinkedGroupDetails, 2L);

        verify(mockLinkedGroupDetailsAuditMapper).modelToEntity(currentLinkedGroupDetails);
        verify(mockLinkedGroupDetailsAuditRepository).save(linkedGroupDetailsAudit);

        verify(mockHearingRepository).findById(HEARING_ID_THREE);
        verify(mockHearingRepository).findById(HEARING_ID_FOUR);

        verify(mockLinkedHearingDetailsAuditMapper).modelToEntity(hearingThree, currentLinkedGroupDetails);
        verify(mockLinkedHearingDetailsAuditMapper).modelToEntity(hearingFour, currentLinkedGroupDetails);
        verify(mockLinkedHearingDetailsAuditRepository).save(linkedHearingDetailsAuditHearingThree);
        verify(mockLinkedHearingDetailsAuditRepository).save(linkedHearingDetailsAuditHearingFour);
    }

    @Test
    void processAmendLinkedHearingRequest_shouldNotUnlinkExistingHearings() {
        LinkedGroupDetails currentLinkedGroupDetails =
            createLinkedGroupDetails("name", "GR", STATUS_ACTIVE, "comment", 1L, ORIGINAL_REQUEST_DATE_TIME);

        HearingEntity hearingOne = createHearing(HEARING_ID_ONE, currentLinkedGroupDetails, 1L);
        HearingEntity hearingTwo = createHearing(HEARING_ID_TWO, currentLinkedGroupDetails, 2L);
        final List<HearingEntity> currentHearings = List.of(hearingOne, hearingTwo);

        LinkHearingDetails linkHearingDetailsHearingOne = new LinkHearingDetails(String.valueOf(HEARING_ID_ONE), 1);
        LinkHearingDetails linkHearingDetailsHearingTwo = new LinkHearingDetails(String.valueOf(HEARING_ID_TWO), 2);
        List<LinkHearingDetails> hearingsInGroup = List.of(linkHearingDetailsHearingOne, linkHearingDetailsHearingTwo);

        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        Optional<HearingEntity> hearingOneOptional = Optional.of(hearingOne);
        Optional<HearingEntity> hearingTwoOptional = Optional.of(hearingTwo);

        LinkedHearingDetailsAudit linkedHearingDetailsAuditHearingOne =
            createLinkedHearingDetailsAudit(currentLinkedGroupDetails, hearingOne, 1L);

        LinkedHearingDetailsAudit linkedHearingDetailsAuditHearingTwo =
            createLinkedHearingDetailsAudit(currentLinkedGroupDetails, hearingTwo, 2L);

        LinkedGroupDetailsAudit linkedGroupDetailsAudit = createLinkedGroupDetailsAudit(currentLinkedGroupDetails);

        when(mockLinkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
            .thenReturn(currentLinkedGroupDetails);
        when(mockLinkedGroupDetailsRepository.save(currentLinkedGroupDetails)).thenReturn(currentLinkedGroupDetails);

        when(mockHearingRepository.existsById(HEARING_ID_ONE)).thenReturn(true);
        when(mockHearingRepository.existsById(HEARING_ID_TWO)).thenReturn(true);
        when(mockLinkedHearingValidator.getHearingOrder(linkHearingDetailsHearingOne, hearingLinkGroupRequest))
            .thenReturn(1L);
        when(mockLinkedHearingValidator.getHearingOrder(linkHearingDetailsHearingTwo, hearingLinkGroupRequest))
            .thenReturn(2L);

        when(mockLinkedGroupDetailsAuditMapper.modelToEntity(currentLinkedGroupDetails))
            .thenReturn(linkedGroupDetailsAudit);

        when(mockHearingRepository.findById(HEARING_ID_ONE)).thenReturn(hearingOneOptional);
        when(mockHearingRepository.findById(HEARING_ID_TWO)).thenReturn(hearingTwoOptional);

        when(mockLinkedHearingDetailsAuditMapper.modelToEntity(hearingOne, currentLinkedGroupDetails))
            .thenReturn(linkedHearingDetailsAuditHearingOne);
        when(mockLinkedHearingDetailsAuditMapper.modelToEntity(hearingTwo, currentLinkedGroupDetails))
            .thenReturn(linkedHearingDetailsAuditHearingTwo);

        futureHearingsLinkedHearingGroupService.processAmendLinkedHearingRequest(hearingLinkGroupRequest,
                                                                                 currentHearings,
                                                                                 REQUEST_ID);

        assertHearingHasLinkGroupAndOrder(hearingOne, 1L);
        assertHearingHasLinkGroupAndOrder(hearingTwo, 2L);

        assertLinkedGroupDetailsUpdated(currentLinkedGroupDetails);

        verify(mockLinkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
        verify(mockLinkedGroupDetailsRepository).save(currentLinkedGroupDetails);

        verify(mockHearingRepository).existsById(HEARING_ID_ONE);
        verify(mockHearingRepository).existsById(HEARING_ID_TWO);
        verify(mockLinkedHearingValidator).getHearingOrder(linkHearingDetailsHearingOne, hearingLinkGroupRequest);
        verify(mockLinkedHearingValidator).getHearingOrder(linkHearingDetailsHearingTwo, hearingLinkGroupRequest);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_ONE, currentLinkedGroupDetails, 1L);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_TWO, currentLinkedGroupDetails, 2L);

        verify(mockLinkedGroupDetailsAuditMapper).modelToEntity(currentLinkedGroupDetails);
        verify(mockLinkedGroupDetailsAuditRepository).save(linkedGroupDetailsAudit);

        verify(mockHearingRepository).findById(HEARING_ID_ONE);
        verify(mockHearingRepository).findById(HEARING_ID_TWO);

        verify(mockLinkedHearingDetailsAuditMapper).modelToEntity(hearingOne, currentLinkedGroupDetails);
        verify(mockLinkedHearingDetailsAuditMapper).modelToEntity(hearingTwo, currentLinkedGroupDetails);
        verify(mockLinkedHearingDetailsAuditRepository).save(linkedHearingDetailsAuditHearingOne);
        verify(mockLinkedHearingDetailsAuditRepository).save(linkedHearingDetailsAuditHearingTwo);
    }

    @Test
    void processAmendLinkedHearingResponse_ShouldUnlinkNewHearingsAndRelinkExistingHearings() {
        LinkHearingDetails linkHearingDetailsHearingThree = new LinkHearingDetails(String.valueOf(HEARING_ID_THREE), 1);
        LinkHearingDetails linkHearingDetailsHearingFour = new LinkHearingDetails(String.valueOf(HEARING_ID_FOUR), 2);
        List<LinkHearingDetails> hearingsInGroup =
            List.of(linkHearingDetailsHearingThree, linkHearingDetailsHearingFour);

        final HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        Map<Long, Long> currentHearings = new HashMap<>();
        currentHearings.put(HEARING_ID_ONE, 1L);
        currentHearings.put(HEARING_ID_TWO, 2L);

        LinkedGroupDetails updatedLinkedGroupDetails =
            createLinkedGroupDetails("new name", "NGR", STATUS_PENDING, "new comment", 2L, NEW_REQUEST_DATE_TIME);

        final PreviousLinkedGroupDetails previousLinkedGroupDetails = createPreviousLinkedGroupDetails();

        LinkedGroupDetails savedUpdatedLinkedGroupDetails = cloneLinkedGroupDetails(updatedLinkedGroupDetails);

        when(mockHearingRepository.existsById(HEARING_ID_ONE)).thenReturn(true);
        when(mockHearingRepository.existsById(HEARING_ID_TWO)).thenReturn(true);

        when(mockLinkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
            .thenReturn(savedUpdatedLinkedGroupDetails);

        futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(hearingLinkGroupRequest,
                                                                                  currentHearings,
                                                                                  updatedLinkedGroupDetails,
                                                                                  previousLinkedGroupDetails);

        assertLinkedGroupDetailsNotUpdated(savedUpdatedLinkedGroupDetails);

        verify(mockLinkedGroupDetailsAuditRepository).deleteLinkedGroupDetailsAudit(LINKED_GROUP_ID, 2L);
        verify(mockLinkedHearingDetailsAuditRepository).deleteLinkedHearingsDetailsAudit(LINKED_GROUP_ID, 2L);

        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_THREE);
        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_FOUR);

        verify(mockHearingRepository).existsById(HEARING_ID_ONE);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_ONE, updatedLinkedGroupDetails, 1L);
        verify(mockHearingRepository).existsById(HEARING_ID_TWO);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_TWO, updatedLinkedGroupDetails, 2L);

        verify(mockLinkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
        verify(mockLinkedGroupDetailsRepository).save(savedUpdatedLinkedGroupDetails);
    }

    @Test
    void processAmendLinkedHearingResponse_ShouldRelinkExistingHearings() {
        LinkHearingDetails linkHearingDetailsHearingOne = new LinkHearingDetails(String.valueOf(HEARING_ID_ONE), 1);
        LinkHearingDetails linkHearingDetailsHearingTwo = new LinkHearingDetails(String.valueOf(HEARING_ID_TWO), 2);
        List<LinkHearingDetails> hearingsInGroup = List.of(linkHearingDetailsHearingOne, linkHearingDetailsHearingTwo);

        final HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        Map<Long, Long> currentHearings = new HashMap<>();
        currentHearings.put(HEARING_ID_ONE, 1L);
        currentHearings.put(HEARING_ID_TWO, 2L);

        LinkedGroupDetails updatedLinkedGroupDetails =
            createLinkedGroupDetails("new name", "NGR", STATUS_PENDING, "new comment", 2L, NEW_REQUEST_DATE_TIME);

        final PreviousLinkedGroupDetails previousLinkedGroupDetails = createPreviousLinkedGroupDetails();

        LinkedGroupDetails savedUpdatedLinkedGroupDetails = cloneLinkedGroupDetails(updatedLinkedGroupDetails);

        when(mockHearingRepository.existsById(HEARING_ID_ONE)).thenReturn(true);
        when(mockHearingRepository.existsById(HEARING_ID_TWO)).thenReturn(true);

        when(mockLinkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
            .thenReturn(savedUpdatedLinkedGroupDetails);

        futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(hearingLinkGroupRequest,
                                                                                  currentHearings,
                                                                                  updatedLinkedGroupDetails,
                                                                                  previousLinkedGroupDetails);

        assertLinkedGroupDetailsNotUpdated(savedUpdatedLinkedGroupDetails);

        verify(mockLinkedGroupDetailsAuditRepository).deleteLinkedGroupDetailsAudit(LINKED_GROUP_ID, 2L);
        verify(mockLinkedHearingDetailsAuditRepository).deleteLinkedHearingsDetailsAudit(LINKED_GROUP_ID, 2L);

        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_ONE);
        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_TWO);

        verify(mockHearingRepository).existsById(HEARING_ID_ONE);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_ONE, updatedLinkedGroupDetails, 1L);
        verify(mockHearingRepository).existsById(HEARING_ID_TWO);
        verify(mockHearingRepository).updateLinkedGroupDetailsAndOrder(HEARING_ID_TWO, updatedLinkedGroupDetails, 2L);

        verify(mockLinkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
        verify(mockLinkedGroupDetailsRepository).save(savedUpdatedLinkedGroupDetails);
    }

    @Test
    void deleteLinkedHearingGroups_shouldRemoveLinkedGroupAndOrder() {
        LinkHearingDetails linkHearingDetailsHearingOne = new LinkHearingDetails(String.valueOf(HEARING_ID_ONE), 1);
        LinkHearingDetails linkHearingDetailsHearingTwo = new LinkHearingDetails(String.valueOf(HEARING_ID_TWO), 2);
        List<LinkHearingDetails> hearingsInGroup = List.of(linkHearingDetailsHearingOne, linkHearingDetailsHearingTwo);
        final HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        LinkedGroupDetails linkedGroupDetails =
            createLinkedGroupDetails("name", "GR", STATUS_PENDING, "comment", 1L, ORIGINAL_REQUEST_DATE_TIME);

        when(mockHearingRepository.existsById(HEARING_ID_ONE)).thenReturn(true);
        when(mockHearingRepository.existsById(HEARING_ID_TWO)).thenReturn(true);
        when(mockLinkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(REQUEST_ID))
            .thenReturn(linkedGroupDetails);

        futureHearingsLinkedHearingGroupService.deleteLinkedHearingGroups(REQUEST_ID, hearingLinkGroupRequest);

        verify(mockHearingRepository).existsById(HEARING_ID_ONE);
        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_ONE);
        verify(mockHearingRepository).existsById(HEARING_ID_TWO);
        verify(mockHearingRepository).removeLinkedGroupDetailsAndOrder(HEARING_ID_TWO);
        verify(mockLinkedGroupDetailsRepository).getLinkedGroupDetailsByRequestId(REQUEST_ID);
        verify(mockLinkedGroupDetailsRepository).delete(linkedGroupDetails);
    }

    @Test
    void deleteLinkedHearingGroups_shouldThrowExceptionIfHearingDoesNotExist() {
        LinkHearingDetails linkHearingDetailsHearingOne = new LinkHearingDetails(String.valueOf(HEARING_ID_ONE), 1);
        List<LinkHearingDetails> hearingsInGroup = List.of(linkHearingDetailsHearingOne);
        HearingLinkGroupRequest hearingLinkGroupRequest = createHearingLinkGroupRequest(hearingsInGroup);

        when(mockHearingRepository.existsById(HEARING_ID_ONE)).thenReturn(false);

        HearingNotFoundException exception =
            assertThrows(HearingNotFoundException.class,
                         () -> futureHearingsLinkedHearingGroupService
                             .deleteLinkedHearingGroups(REQUEST_ID, hearingLinkGroupRequest),
                     "Hearing not found exception should be thrown if hearing does not exist");

        String expectedMessage = "No hearing found for reference: " + HEARING_ID_ONE;
        assertEquals(expectedMessage, exception.getMessage(), "Hearing not found exception has unexpected message");

        verify(mockHearingRepository).existsById(HEARING_ID_ONE);
    }

    private LinkedGroupDetails createLinkedGroupDetails(String name,
                                                        String reason,
                                                        String status,
                                                        String comments,
                                                        long latestVersion,
                                                        LocalDateTime requestDateTime) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();

        linkedGroupDetails.setLinkedGroupId(LINKED_GROUP_ID);
        linkedGroupDetails.setRequestId(REQUEST_ID);
        linkedGroupDetails.setRequestName(name);
        linkedGroupDetails.setLinkType(LinkType.ORDERED);
        linkedGroupDetails.setReasonForLink(reason);
        linkedGroupDetails.setStatus(status);
        linkedGroupDetails.setLinkedComments(comments);
        linkedGroupDetails.setLinkedGroupLatestVersion(latestVersion);
        linkedGroupDetails.setRequestDateTime(requestDateTime);

        return linkedGroupDetails;
    }

    private LinkedGroupDetails cloneLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails) {
        LinkedGroupDetails clonedLinkGroupDetails = new LinkedGroupDetails();

        clonedLinkGroupDetails.setLinkedGroupId(linkedGroupDetails.getLinkedGroupId());
        clonedLinkGroupDetails.setRequestId(linkedGroupDetails.getRequestId());
        clonedLinkGroupDetails.setRequestName(linkedGroupDetails.getRequestName());
        clonedLinkGroupDetails.setLinkType(linkedGroupDetails.getLinkType());
        clonedLinkGroupDetails.setReasonForLink(linkedGroupDetails.getReasonForLink());
        clonedLinkGroupDetails.setStatus(linkedGroupDetails.getStatus());
        clonedLinkGroupDetails.setLinkedComments(linkedGroupDetails.getLinkedComments());
        clonedLinkGroupDetails.setLinkedGroupLatestVersion(linkedGroupDetails.getLinkedGroupLatestVersion());
        clonedLinkGroupDetails.setRequestDateTime(linkedGroupDetails.getRequestDateTime());

        return clonedLinkGroupDetails;
    }

    private HearingEntity createHearing(long id) {
        return createHearing(id, null, null);
    }

    private HearingEntity createHearing(long id, LinkedGroupDetails linkedGroupDetails, Long linkedOrder) {
        HearingEntity hearing = new HearingEntity();

        hearing.setId(id);
        hearing.setLinkedGroupDetails(linkedGroupDetails);
        hearing.setLinkedOrder(linkedOrder);

        return hearing;
    }

    private HearingLinkGroupRequest createHearingLinkGroupRequest(List<LinkHearingDetails> hearingsInGroup) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupName("new name");
        groupDetails.setGroupReason("NGR");
        groupDetails.setGroupLinkType("ORDERED");
        groupDetails.setGroupComments("new comment");

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setGroupDetails(groupDetails);
        hearingLinkGroupRequest.setHearingsInGroup(hearingsInGroup);

        return hearingLinkGroupRequest;
    }

    private LinkedHearingDetailsAudit createLinkedHearingDetailsAudit(LinkedGroupDetails linkedGroupDetails,
                                                                      HearingEntity hearing,
                                                                      Long linkedOrder) {
        LinkedHearingDetailsAudit linkedHearingDetailsAudit = new LinkedHearingDetailsAudit();

        linkedHearingDetailsAudit.setLinkedGroup(linkedGroupDetails);
        linkedHearingDetailsAudit.setLinkedGroupVersion(2L);
        linkedHearingDetailsAudit.setHearing(hearing);
        linkedHearingDetailsAudit.setLinkedOrder(linkedOrder);

        return linkedHearingDetailsAudit;
    }

    private LinkedGroupDetailsAudit createLinkedGroupDetailsAudit(LinkedGroupDetails linkedGroupDetails) {
        LinkedGroupDetailsAudit linkedGroupDetailsAudit = new LinkedGroupDetailsAudit();

        linkedGroupDetailsAudit.setLinkedGroup(linkedGroupDetails);
        linkedGroupDetailsAudit.setRequestId(REQUEST_ID);
        linkedGroupDetailsAudit.setRequestName("new name");
        linkedGroupDetailsAudit.setLinkedGroupVersion(2L);
        linkedGroupDetailsAudit.setLinkType(LinkType.ORDERED);
        linkedGroupDetailsAudit.setReasonForLink("NGR");
        linkedGroupDetailsAudit.setStatus("PENDING");
        linkedGroupDetailsAudit.setLinkedComments("new comment");

        return linkedGroupDetailsAudit;
    }

    private PreviousLinkedGroupDetails createPreviousLinkedGroupDetails() {
        PreviousLinkedGroupDetails previousLinkedGroupDetails = new PreviousLinkedGroupDetails();

        previousLinkedGroupDetails.setLinkedGroupId(LINKED_GROUP_ID);
        previousLinkedGroupDetails.setRequestId(REQUEST_ID);
        previousLinkedGroupDetails.setRequestName("name");
        previousLinkedGroupDetails.setLinkType(LinkType.ORDERED);
        previousLinkedGroupDetails.setReasonForLink("GR");
        previousLinkedGroupDetails.setStatus("ACTIVE");
        previousLinkedGroupDetails.setLinkedComments("comment");
        previousLinkedGroupDetails.setLinkedGroupLatestVersion(1L);
        previousLinkedGroupDetails.setRequestDateTime(ORIGINAL_REQUEST_DATE_TIME);

        return previousLinkedGroupDetails;
    }

    private void assertHearingHasNoLinkGroupAndOrder(HearingEntity hearing) {
        String errorMessagePrefix = "Hearing " + hearing.getId() + " ";

        assertNull(hearing.getLinkedGroupDetails(), errorMessagePrefix + "linked group details should be null");
        assertNull(hearing.getLinkedOrder(), errorMessagePrefix + "linked order should be null");
    }

    private void assertHearingHasLinkGroupAndOrder(HearingEntity hearing, Long expectedLinkedOrder) {
        String errorMessagePrefix = "Hearing " + hearing.getId() + " ";

        LinkedGroupDetails linkedGroupDetails = hearing.getLinkedGroupDetails();
        assertNotNull(linkedGroupDetails, errorMessagePrefix + "should have linked group");
        assertEquals(REQUEST_ID,
                     linkedGroupDetails.getRequestId(),
                     errorMessagePrefix + "has unexpected linked group request id");

        Long actualLinkedOrder = hearing.getLinkedOrder();
        assertNotNull(actualLinkedOrder, errorMessagePrefix + "should have linked order");
        assertEquals(expectedLinkedOrder, actualLinkedOrder, errorMessagePrefix + "has unexpected linked order");
    }

    private void assertLinkedGroupDetailsUpdated(LinkedGroupDetails linkedGroupDetails) {
        assertLinkedGroupDetails(linkedGroupDetails, 2L, "new name", "NGR", "new comment", STATUS_PENDING);
        assertTrue(linkedGroupDetails.getRequestDateTime().isAfter(ORIGINAL_REQUEST_DATE_TIME),
                   "Linked group details request date time should have been updated");
    }

    private void assertLinkedGroupDetailsNotUpdated(LinkedGroupDetails linkedGroupDetails) {
        assertLinkedGroupDetails(linkedGroupDetails, 1L, "name", "GR", "comment", STATUS_ACTIVE);
        assertEquals(ORIGINAL_REQUEST_DATE_TIME,
                     linkedGroupDetails.getRequestDateTime(),
                     "Linked group details has unexpected request date time");
    }

    private void assertLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails,
                                          Long expectedLatestVersion,
                                          String expectedName,
                                          String expectedReason,
                                          String expectedComment,
                                          String expectedStatus) {
        assertEquals(REQUEST_ID, linkedGroupDetails.getRequestId(), "Linked group details has unexpected request id");
        assertEquals(expectedLatestVersion,
                     linkedGroupDetails.getLinkedGroupLatestVersion(),
                     "Linked group details has unexpected latest version");
        assertEquals(expectedName,
                     linkedGroupDetails.getRequestName(),
                     "Linked group details has unexpected request name");
        assertEquals(expectedReason,
                     linkedGroupDetails.getReasonForLink(),
                     "Linked group details has unexpected reason for link");
        assertEquals(LinkType.ORDERED,
                     linkedGroupDetails.getLinkType(),
                     "Linked group details has unexpected link type");
        assertEquals(expectedComment,
                     linkedGroupDetails.getLinkedComments(),
                     "Linked group details has unexpected linked comments");
        assertEquals(expectedStatus, linkedGroupDetails.getStatus(), "Linked group details has unexpected status");
    }
}
