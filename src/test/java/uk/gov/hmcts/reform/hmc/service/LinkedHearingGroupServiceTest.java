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
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkedHearingGroupResponses;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType.ORDERED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class LinkedHearingGroupServiceTest {

    public static final String FOR_DELETE_INVALID_HEARING_STATUS = "HEARING_STATUS_INVALID";
    public static final long HEARING_GROUP_ID = 567L;
    public static final long HEARING_ID1 = 1234L;
    public static final long HEARING_ID2 = 1235L;
    public static final LocalDateTime START_DATE_TIME_IN_THE_FUTURE =
        LocalDateTime.of(2500, 10, 1, 1, 1);
    public static final LocalDateTime START_DATE_TIME_IN_THE_PAST =
        LocalDateTime.of(2000, 10, 1, 1, 1);
    public static final LocalDateTime HEARING_RESPONSE_DATE_TIME = LocalDateTime.now();

    @Mock
    LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Mock
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    HearingRepository hearingRepository;

    @InjectMocks
    private LinkedHearingGroupServiceImpl service;

    @Mock
    LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    @Mock
    LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    @Mock
    LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LinkedHearingGroupServiceImpl(hearingRepository,
                                                    linkedGroupDetailsRepository, linkedHearingDetailsRepository,
                                                    linkedHearingDetailsAuditRepository,
                                                    linkedGroupDetailsAuditRepository,
                                                    linkedGroupDetailsAuditMapper,
                                                    linkedHearingDetailsAuditMapper);
    }

    @Nested
    @DisplayName("deleteHearingGroup")
    class DeleteHearingGroup {

        @Test
        void shouldDeleteHearingGroupDetails() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setLinkedGroupDetails(groupDetails);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setLinkedGroupDetails(groupDetails);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));

            LinkedGroupDetailsAudit groupDetailsAudit = createGroupDetailsAuditEntity(HEARING_GROUP_ID,
                                                                                      "ACTIVE",groupDetails);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));
            given(linkedGroupDetailsAuditMapper.modelToEntity(groupDetails))
                .willReturn(groupDetailsAudit);

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldReturn404ErrorWhenNonExistentHearingGroup() {
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.empty());

            Exception exception = assertThrows(LinkedHearingGroupNotFoundException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("No hearing group found for reference: " + HEARING_GROUP_ID, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusPENDING() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "PENDING");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a PENDING state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenGroupDetailsHasStatusERROR() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ERROR");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a ERROR state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsHEARING_REQUESTEDButPlannedHearingDateInThePast() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID2,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsUPDATE_REQUESTEDButPlannedHearingDateInThePast() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(HEARING_STATUS_UPDATE_REQUESTED);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_PAST)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID1,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldReturn400ErrorWhenHearingStatusIsInvalidForUnlinking() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(FOR_DELETE_INVALID_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("008 Invalid state for unlinking hearing request " + HEARING_ID1,
                         exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldDeleteHearingGroupDetailsFilteringOutInvalidMultipleResponseHearingVersions() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(POST_HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(2, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(POST_HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing1.setLinkedGroupDetails(groupDetails);
            hearing2.setLinkedGroupDetails(groupDetails);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        @Test
        void shouldDeleteHearingGroupDetailsFilteringOutHearingResponsesWithNonRecentTimestampForSameVersion() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(POST_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME.minusDays(1),
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays(1, HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");
            hearing.setLinkedGroupDetails(groupDetails);
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
        }

        private HearingResponseEntity createHearingResponseEntityWithHearingDays(
            Integer requestVersion,
            LocalDateTime requestTimestamp,
            List<LocalDateTime> hearingDaysStartDateTime) {

            HearingResponseEntity hearingResponse = new HearingResponseEntity();
            hearingResponse.setRequestVersion(requestVersion);
            hearingResponse.setRequestTimeStamp(requestTimestamp);
            hearingResponse.setHearingDayDetails(
                hearingDaysStartDateTime.stream().map(this::createHearingDayDetails).collect(Collectors.toList())
            );
            return hearingResponse;
        }

        private HearingDayDetailsEntity createHearingDayDetails(LocalDateTime hearingDayStartDateTime) {
            HearingDayDetailsEntity hearingDayDetails1 = new HearingDayDetailsEntity();
            hearingDayDetails1.setStartDateTime(hearingDayStartDateTime);
            return hearingDayDetails1;
        }

        private LinkedGroupDetails createGroupDetailsEntity(Long hearingGroupId, String groupStatus) {
            LinkedGroupDetails groupDetails = new LinkedGroupDetails();
            groupDetails.setLinkedGroupId(hearingGroupId);
            groupDetails.setStatus(groupStatus);
            groupDetails.setLinkedGroupLatestVersion(1L);
            return groupDetails;
        }

        private LinkedGroupDetailsAudit createGroupDetailsAuditEntity(long hearingGroupId, String groupStatus,
                                                                      LinkedGroupDetails groupDetails) {
            LinkedGroupDetailsAudit groupDetailsAudit = new LinkedGroupDetailsAudit();
            groupDetailsAudit.setLinkedGroup(groupDetails);
            groupDetailsAudit.setLinkedGroupVersion(1L);
            groupDetailsAudit.setLinkType(ORDERED);
            groupDetailsAudit.setStatus(groupStatus);
            return groupDetailsAudit;
        }

    }

    @Nested
    @DisplayName("getHearingGroup")
    class GetHearingGroup {
        public static final String INVALID_REQUEST_NAME = "Invalid Name";
        public static final String VALID_REQUEST_ID = "Request Name";

        @Test
        void shouldGetLinkedHearingGroupDetails() {
            LinkedGroupDetails linkedGroupDetails =
                generateLinkedGroupDetails(HEARING_GROUP_ID);
            HearingEntity hearing1 = generateHearingEntity(HEARING_ID1,linkedGroupDetails);
            HearingEntity hearing2 = generateHearingEntity(HEARING_ID2,linkedGroupDetails);

            given(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(VALID_REQUEST_ID))
                .willReturn(linkedGroupDetails);
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            LinkedHearingGroupResponses linkedHearingGroupResponses =
                service.getLinkedHearingGroupDetails(VALID_REQUEST_ID);
            GroupDetails returnedGroupDetails = linkedHearingGroupResponses.getGroupDetails();
            assertFalse(
                returnedGroupDetails != null
                    && returnedGroupDetails.getGroupName().isEmpty()
                    && returnedGroupDetails.getGroupComments().isEmpty());
        }

        @Test
        void shouldReturn404ErrorWhenRequestIdIsNotFound() {
            Exception exception = assertThrows(LinkedGroupNotFoundException.class, () ->
                service.getLinkedHearingGroupDetails(INVALID_REQUEST_NAME));
            assertEquals(INVALID_LINKED_GROUP_REQUEST_ID_DETAILS,exception.getMessage());
        }

        private LinkedGroupDetails generateLinkedGroupDetails(Long hearingGroupId) {
            LinkedGroupDetails groupDetails = new LinkedGroupDetails();
            groupDetails.setLinkedGroupId(hearingGroupId);
            groupDetails.setLinkType(LinkType.ORDERED);
            groupDetails.setReasonForLink("reason for link");
            groupDetails.setRequestDateTime(LocalDateTime.now());
            groupDetails.setRequestId("2B");
            groupDetails.setRequestName(VALID_REQUEST_ID);
            groupDetails.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
            return groupDetails;
        }

        private HearingEntity generateHearingEntity(Long hearingId, LinkedGroupDetails groupDetails) {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(hearingId);
            hearing.setLinkedGroupDetails(groupDetails);
            hearing.setStatus("ACTIVE");
            hearing.setIsLinkedFlag(true);
            hearing.setLinkedOrder(1L);
            return hearing;
        }

    }
}
