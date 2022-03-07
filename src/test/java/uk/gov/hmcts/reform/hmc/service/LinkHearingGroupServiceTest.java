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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus.HEARING_REQUESTED;

@ExtendWith(MockitoExtension.class)
class LinkHearingGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LinkHearingGroupServiceTest.class);

    @InjectMocks
    private LinkedHearingGroupServiceImpl linkedHearingGroupService;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Mock
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingGroupService =
            new LinkedHearingGroupServiceImpl(
                hearingRepository,
                    linkedGroupDetailsRepository,
                        linkedHearingDetailsRepository
            );
    }

    @Nested
    @DisplayName("postLinkedHearingGroup")
    class PostLinkedHearingGroup {
        @Test
        void shouldFailWithHearingNotFound() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(false);

            Exception exception = assertThrows(HearingNotFoundException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInsufficientRequestIds() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000000", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("001 Insufficient requestIds", exception.getMessage());
        }


        @Test
        void shouldFailWithHearingRequestIsLinkedIsFalse() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1,
                false,
                LocalDate.now()
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("002 hearing request isLinked is False", exception.getMessage());
        }


        @Test
        void shouldFailWithHearingRequestAlreadyInGroup() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1,
                true,
                LocalDate.now()
            );

            LinkedGroupDetails groupDetails1 = generateLinkGroupDetails(
                200L,
                "requestId",
                "requestname",
                "linkTYpe",
                "status",
                "resaon",
                "comments",
                LocalDateTime.of(2022, 03, 02, 10, 11)
            );
            LinkedHearingDetails linkedHearingDetails = generateLinkHearingDetails(
                2000000000L,
                hearingEntity,
                23L,
                groupDetails1
            );
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000000L)).thenReturn(
                linkedHearingDetails);

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("003 hearing request already in a group", exception.getMessage());
        }


        @Test
        void shouldFailWithInvalidState() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                "status",
                1,
                true,
                LocalDate.now()
            );

            LinkedHearingDetails linkedHearingDetails = generateLinkHearingDetails(
                2000000000L,
                hearingEntity,
                23L,
                null
            );
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000000L)).thenReturn(
                linkedHearingDetails);

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidDate() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDate.now().minusDays(1)
            );

            LinkedHearingDetails linkedHearingDetails = generateLinkHearingDetails(
                2000000000L,
                hearingEntity,
                23L,
                null
            );
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000000L)).thenReturn(
                linkedHearingDetails);

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingOrderIsNotUnique() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails1,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDate.now().plusDays(1)
            );

            LinkedHearingDetails linkedHearingDetails = generateLinkHearingDetails(
                2000000000L,
                hearingEntity,
                23L,
                null
            );
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000000L)).thenReturn(
                linkedHearingDetails);

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("005 Hearing Order is not unique", exception.getMessage());
        }


        @Test
        void shouldPassWithValidLinkedHearing() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             LinkType.ORDERED, "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDate.now().plusDays(1)
            );

            LinkedHearingDetails linkedHearingDetails = generateLinkHearingDetails(
                2000000000L,
                hearingEntity,
                23L,
                null
            );
            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000000L)).thenReturn(
                linkedHearingDetails);

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));
            when(linkedHearingDetailsRepository.getLinkedHearingDetailsById(2000000002L)).thenReturn(
                linkedHearingDetails);

            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                    groupDetails,
                    Arrays.asList(
                            hearingDetails1,
                            hearingDetails2
                    )
            );
            linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            verify(hearingRepository).existsById(2000000000L);
            verify(hearingRepository).findById(2000000000L);
            verify(linkedHearingDetailsRepository).getLinkedHearingDetailsById(2000000000L);
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
            verify(linkedHearingDetailsRepository).getLinkedHearingDetailsById(2000000002L);
        }
    }

    private HearingLinkGroupRequest generateHearingLink(GroupDetails groupDetails,
                                                        List<LinkHearingDetails> hearingDetails) {

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setHearingsInGroup(hearingDetails);
        hearingLinkGroupRequest.setGroupDetails(groupDetails);

        return hearingLinkGroupRequest;
    }

    private GroupDetails generateGroupDetails(String groupComments, String groupName, LinkType linktype,
                                              String groupReason) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupComments(groupComments);
        groupDetails.setGroupName(groupName);
        groupDetails.setGroupLinkType(linktype);
        groupDetails.setGroupReason(groupReason);
        return groupDetails;
    }

    private LinkHearingDetails generateHearingDetails(String hearingId, int order) {
        LinkHearingDetails hearingDetails = new LinkHearingDetails();
        hearingDetails.setHearingId(hearingId);
        hearingDetails.setHearingOrder(order);
        return hearingDetails;
    }

    private HearingEntity generateHearingEntity(Long hearingId, String status, Integer versionNumber,
                                                boolean isLinked, LocalDate hearingDate) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setVersionNumber(versionNumber);
        caseHearingRequestEntity.setIsLinkedFlag(isLinked);
        caseHearingRequestEntity.setHearingWindowStartDateRange(hearingDate);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private LinkedHearingDetails generateLinkHearingDetails(Long hearingId, HearingEntity hearingEntity,
                                                            Long order, LinkedGroupDetails linkedGroupDetails) {
        LinkedHearingDetails linkedHearingDetails = new LinkedHearingDetails();
        linkedHearingDetails.setHearing(hearingEntity);
        linkedHearingDetails.setLinkedGroup(linkedGroupDetails);
        linkedHearingDetails.setLinkedOrder(order);
        linkedHearingDetails.setLinkedHearingId(hearingId);

        return linkedHearingDetails;
    }

    private LinkedGroupDetails generateLinkGroupDetails(Long linkGroupId, String requestId, String requestName,
                                                        String linkType, String status, String reason,
                                                        String comments, LocalDateTime date) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(linkGroupId);
        linkedGroupDetails.setLinkType(linkType);
        linkedGroupDetails.setLinkedComments(comments);
        linkedGroupDetails.setRequestDateTime(date);
        linkedGroupDetails.setReasonForLink(reason);
        linkedGroupDetails.setStatus(status);
        linkedGroupDetails.setRequestId(requestId);
        linkedGroupDetails.setRequestName(requestName);

        return linkedGroupDetails;
    }


}
