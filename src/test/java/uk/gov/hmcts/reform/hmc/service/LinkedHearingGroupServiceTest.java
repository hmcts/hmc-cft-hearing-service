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
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class LinkedHearingGroupServiceTest {

    public static final long HEARING_GROUP_ID = 567L;
    public static final long HEARING_ID1 = 1234L;
    public static final long HEARING_ID2 = 1235L;
    public static final LocalDateTime START_DATE_TIME_IN_THE_FUTURE =
        LocalDateTime.of(2500, 10, 1, 1, 1);
    public static final LocalDateTime START_DATE_TIME_IN_THE_PAST =
        LocalDateTime.of(2000, 10, 1, 1, 1);
    public static final LocalDateTime HEARING_RESPONSE_DATE_TIME = LocalDateTime.now();

    @Mock
    LinkedHearingValidator linkedHearingValidator;

    @Mock
    LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Mock
    HearingRepository hearingRepository;

    @InjectMocks
    private LinkedHearingGroupServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LinkedHearingGroupServiceImpl(hearingRepository, linkedHearingValidator);
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

            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);
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

            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing1, hearing2));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);
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

            given(hearingRepository.findByLinkedGroupId(HEARING_GROUP_ID))
                .willReturn(List.of(hearing));

            service.deleteLinkedHearingGroup(HEARING_GROUP_ID);
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
            return groupDetails;
        }
    }
}
