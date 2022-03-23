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
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS_UPDATE_REQUESTED;

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
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    HearingRepository hearingRepository;

    @InjectMocks
    private LinkedHearingGroupServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LinkedHearingGroupServiceImpl(linkedGroupDetailsRepository, hearingRepository);
    }

    @Nested
    @DisplayName("deleteHearingGroup")
    class DeleteHearingGroup {

        @Test
        public void shouldDeleteHearingGroupDetails() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setLinkedGroupDetails(groupDetails);
            hearing1.setStatus(HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setLinkedGroupDetails(groupDetails);
            hearing2.setStatus(HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1)).deleteHearingGroup(HEARING_GROUP_ID);
        }

        @Test
        public void shouldReturn404ErrorWhenNonExistentHearingGroup() {
            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID)).willReturn(Optional.empty());

            Exception exception = assertThrows(LinkedHearingGroupNotFoundException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("No hearing group found for reference: " + HEARING_GROUP_ID, exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldReturn400ErrorWhenGroupDetailsHasStatusPENDING() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "PENDING");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a PENDING state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldReturn400ErrorWhenGroupDetailsHasStatusERROR() {
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ERROR");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));

            Exception exception = assertThrows(BadRequestException.class, () ->
                service.deleteLinkedHearingGroup(HEARING_GROUP_ID));
            assertEquals("007 group is in a ERROR state", exception.getMessage());
            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, never()).findByLinkedGroupId(anyLong());
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldReturn400ErrorWhenHearingStatusIsHEARING_REQUESTEDButPlannedHearingDateInThePast() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE,
                                                                   START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
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
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldReturn400ErrorWhenHearingStatusIsUPDATE_REQUESTEDButPlannedHearingDateInThePast() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(HEARING_STATUS_UPDATE_REQUESTED);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
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
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldReturn400ErrorWhenHearingStatusIsInvalidForUnlinking() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(FOR_DELETE_INVALID_HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
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
            verify(linkedGroupDetailsRepository, never()).deleteHearingGroup(anyLong());
        }

        @Test
        public void shouldDeleteHearingGroupDetailsFilteringOutInvalidMultipleResponseHearingVersions() {

            HearingEntity hearing1 = new HearingEntity();
            hearing1.setId(HEARING_ID1);
            hearing1.setStatus(HEARING_STATUS);
            hearing1.setIsLinkedFlag(true);
            hearing1.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("2", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                ),
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST,
                                                                   START_DATE_TIME_IN_THE_FUTURE)
                )));

            HearingEntity hearing2 = new HearingEntity();
            hearing2.setId(HEARING_ID2);
            hearing2.setStatus(HEARING_STATUS);
            hearing2.setIsLinkedFlag(true);
            hearing2.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1)).deleteHearingGroup(HEARING_GROUP_ID);
        }

        @Test
        public void shouldDeleteHearingGroupDetailsFilteringOutHearingResponsesWithNonRecentTimestampForSameVersion() {

            HearingEntity hearing = new HearingEntity();
            hearing.setId(HEARING_ID1);
            hearing.setStatus(HEARING_STATUS);
            hearing.setIsLinkedFlag(true);
            hearing.setHearingResponses(List.of(
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME.minusDays(1),
                                                           // should not fail as will get filtered out
                                                           List.of(START_DATE_TIME_IN_THE_PAST)
                ),
                createHearingResponseEntityWithHearingDays("1", HEARING_RESPONSE_DATE_TIME,
                                                           List.of(START_DATE_TIME_IN_THE_FUTURE)
                )));
            LinkedGroupDetails groupDetails = createGroupDetailsEntity(HEARING_GROUP_ID, "ACTIVE");

            given(linkedGroupDetailsRepository.findById(HEARING_GROUP_ID))
                .willReturn(Optional.of(groupDetails));
            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);

            verify(linkedGroupDetailsRepository, times(1)).findById(HEARING_GROUP_ID);
            verify(hearingRepository, times(1)).findByLinkedGroupId(HEARING_GROUP_ID);
            verify(linkedGroupDetailsRepository, times(1)).deleteHearingGroup(HEARING_GROUP_ID);
        }

        private HearingResponseEntity createHearingResponseEntityWithHearingDays(
            String requestVersion,
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
            return groupDetails;
        }
    }
}
