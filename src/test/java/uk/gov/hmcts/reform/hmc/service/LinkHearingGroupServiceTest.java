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
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

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

    @InjectMocks
    private LinkedHearingGroupServiceImpl linkedHearingGroupService;

    @Mock
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingGroupService =
            new LinkedHearingGroupServiceImpl(
                hearingRepository
            );
    }

    @Nested
    @DisplayName("postLinkedHearingGroup")
    class PostLinkedHearingGroup {
        @Test
        void shouldFailWithHearingNotFound() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
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
                                                             "Ordered", "reason"
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
                                                             "Ordered", "reason"
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
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                null
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
                                                             "Ordered", "reason"
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

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                DeleteHearingStatus.UPDATE_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                groupDetails1
            );


            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("003 hearing request already in a group", exception.getMessage());
        }


        @Test
        void shouldFailWithInvalidState() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
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
                LocalDateTime.now(),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now())),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithInvalidDate() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
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


            HearingDayDetailsEntity hearingDayDetailsEntity =
                generateHearingDetailsEntity(
                    2000000002L,
                    LocalDateTime.of(2020, 11, 11, 12, 1)
                );
            HearingDayDetailsEntity hearingDayDetailsEntity1 =
                generateHearingDetailsEntity(
                    2000000002L,
                    LocalDateTime.of(2021, 11, 11, 12, 1)
                );
            HearingDayDetailsEntity hearingDayDetailsEntity2 =
                generateHearingDetailsEntity(
                    2000000000L,
                    LocalDateTime.of(2022, 11, 11, 12, 1)
                );


            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().minusDays(1),
                Arrays.asList(hearingDayDetailsEntity, hearingDayDetailsEntity1, hearingDayDetailsEntity2),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("004 Invalid state for hearing request 2000000000", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingOrderIsNotUnique() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
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
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("005 Hearing Order is not unique", exception.getMessage());
        }

        @Test
        void shouldFailWithNoHearingOrderWhenLinkTypeIsOrdered() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
            );
            LinkHearingDetails hearingDetails = new LinkHearingDetails();
            hearingDetails.setHearingId("2000000000");
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
            HearingLinkGroupRequest hearingLinkGroupRequest = generateHearingLink(
                groupDetails,
                Arrays.asList(
                    hearingDetails,
                    hearingDetails2
                )
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("Hearing order must exist and be greater than 0", exception.getMessage());
        }

        @Test
        void shouldFailWithHearingOrderInvalidValue() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered One", "reason"
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
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            Exception exception = assertThrows(BadRequestException.class, () -> {
                linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
            });
            assertEquals("Invalid value for GroupLinkType", exception.getMessage());
        }

        @Test
        void shouldPassWhenHearingOrderIsSameSlot() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Same Slot", "reason"
            );

            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 1);
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
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
        }

        @Test
        void shouldPassWithValidLinkedHearing() {
            GroupDetails groupDetails = generateGroupDetails("comment", "name",
                                                             "Ordered", "reason"
            );
            LinkHearingDetails hearingDetails1 = generateHearingDetails("2000000000", 1);
            LinkHearingDetails hearingDetails2 = generateHearingDetails("2000000002", 2);
            HearingEntity hearingEntity = generateHearingEntity(
                2000000000L,
                HEARING_REQUESTED.name(),
                1,
                true,
                LocalDateTime.now().plusDays(1),
                Arrays.asList(generateHearingDetailsEntity(2000000002L, LocalDateTime.now().plusDays(1))),
                null
            );

            when(hearingRepository.existsById(2000000000L)).thenReturn(true);
            when(hearingRepository.findById(2000000000L)).thenReturn(Optional.of(hearingEntity));

            when(hearingRepository.existsById(2000000002L)).thenReturn(true);
            when(hearingRepository.findById(2000000002L)).thenReturn(Optional.of(hearingEntity));

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
            verify(hearingRepository).existsById(2000000002L);
            verify(hearingRepository).findById(2000000002L);
        }
    }

    private HearingLinkGroupRequest generateHearingLink(GroupDetails groupDetails,
                                                        List<LinkHearingDetails> hearingDetails) {

        HearingLinkGroupRequest hearingLinkGroupRequest = new HearingLinkGroupRequest();
        hearingLinkGroupRequest.setHearingsInGroup(hearingDetails);
        hearingLinkGroupRequest.setGroupDetails(groupDetails);

        return hearingLinkGroupRequest;
    }

    private GroupDetails generateGroupDetails(String groupComments, String groupName, String linktype,
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
                                                boolean isLinked, LocalDateTime requestTimestamp,
                                                List<HearingDayDetailsEntity> hearingDayDetailsEntities,
                                                LinkedGroupDetails linkedGroupDetails) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);
        hearingEntity.setIsLinkedFlag(isLinked);
        hearingEntity.setLinkedGroupDetails(linkedGroupDetails);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setVersionNumber(versionNumber);

        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);

        HearingResponseEntity hearingResponseEntity = new HearingResponseEntity();
        hearingResponseEntity.setHearingDayDetails(hearingDayDetailsEntities);
        hearingResponseEntity.setHearing(hearingEntity);
        hearingResponseEntity.setResponseVersion(versionNumber.toString());
        hearingResponseEntity.setRequestTimeStamp(requestTimestamp);

        hearingEntity.setHearingResponses(List.of(hearingResponseEntity));
        return hearingEntity;
    }

    private HearingDayDetailsEntity generateHearingDetailsEntity(Long hearingId, LocalDateTime hearingDateTime) {
        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(hearingDateTime);
        hearingDayDetailsEntity.setHearingDayId(hearingId);
        return hearingDayDetailsEntity;
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
